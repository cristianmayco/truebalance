package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.domain.entity.*;
import com.truebalance.truebalance.domain.exception.CreditCardNotFoundException;
import com.truebalance.truebalance.domain.exception.CreditLimitExceededException;
import com.truebalance.truebalance.domain.port.CreditCardRepositoryPort;
import com.truebalance.truebalance.domain.port.InstallmentRepositoryPort;
import com.truebalance.truebalance.domain.port.InvoiceRepositoryPort;
import com.truebalance.truebalance.domain.service.InstallmentDateCalculator;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Use case to update a bill and link it to a credit card with automatic installment distribution.
 * 
 * This handles:
 * - Updating an existing bill
 * - Removing old installments if bill was previously linked to a card
 * - Creating new installments for the credit card
 * - Updating invoice totals
 * 
 * Business Rules Implemented:
 * - BR-B-004: Distribution of installments across invoices
 * - BR-CC-008: Validation of available credit limit
 * - BR-I-001: Invoice creation (via GenerateOrGetInvoiceForMonth)
 * - BR-I-002: One invoice per credit card per month
 * - BR-I-004: Billing cycle calculation
 * - BR-I-005: Calculation of invoice total amount
 * - BR-INS-001: Installment creation
 * - BR-INS-002: Installment sequencing
 * 
 * CRITICAL: This operation is @Transactional to ensure atomicity.
 * If any step fails, ALL changes are rolled back.
 */
public class UpdateBillWithCreditCard {

    private final UpdateBill updateBill;
    private final CreditCardRepositoryPort creditCardRepository;
    private final InstallmentRepositoryPort installmentRepository;
    private final InvoiceRepositoryPort invoiceRepository;
    private final GenerateOrGetInvoiceForMonth generateOrGetInvoiceForMonth;
    private final GetAvailableLimit getAvailableLimit;
    private final InstallmentDateCalculator installmentDateCalculator;
    private final GetBillInstallments getBillInstallments;

    public UpdateBillWithCreditCard(
            UpdateBill updateBill,
            CreditCardRepositoryPort creditCardRepository,
            InstallmentRepositoryPort installmentRepository,
            InvoiceRepositoryPort invoiceRepository,
            GenerateOrGetInvoiceForMonth generateOrGetInvoiceForMonth,
            GetAvailableLimit getAvailableLimit,
            InstallmentDateCalculator installmentDateCalculator,
            GetBillInstallments getBillInstallments) {
        this.updateBill = updateBill;
        this.creditCardRepository = creditCardRepository;
        this.installmentRepository = installmentRepository;
        this.invoiceRepository = invoiceRepository;
        this.generateOrGetInvoiceForMonth = generateOrGetInvoiceForMonth;
        this.getAvailableLimit = getAvailableLimit;
        this.installmentDateCalculator = installmentDateCalculator;
        this.getBillInstallments = getBillInstallments;
    }

    /**
     * Execute the update of a bill with credit card integration.
     *
     * @param billId       the ID of the bill to update
     * @param bill         the updated bill data
     * @param creditCardId the credit card ID to link to
     * @return the updated bill
     * @throws CreditCardNotFoundException   if credit card not found
     * @throws CreditLimitExceededException if available limit is insufficient
     */
    @Transactional(rollbackFor = Exception.class)
    public Bill execute(Long billId, Bill bill, Long creditCardId) {
        // 1. Validate credit card exists
        CreditCard creditCard = creditCardRepository.findById(creditCardId)
                .orElseThrow(() -> new CreditCardNotFoundException(creditCardId));

        // 2. Get existing installments (if any) to update invoices later
        List<Installment> existingInstallments = getBillInstallments.execute(billId);
        
        // 3. Remove old installments and update old invoices
        if (!existingInstallments.isEmpty()) {
            // Group installments by invoice to update invoice totals
            Map<Long, BigDecimal> invoiceAmountsToSubtract = new HashMap<>();
            for (Installment installment : existingInstallments) {
                invoiceAmountsToSubtract.merge(
                    installment.getInvoiceId(),
                    installment.getAmount(),
                    BigDecimal::add
                );
            }
            
            // Update old invoices by subtracting old installment amounts
            for (Map.Entry<Long, BigDecimal> entry : invoiceAmountsToSubtract.entrySet()) {
                invoiceRepository.findById(entry.getKey()).ifPresent(invoice -> {
                    invoice.setTotalAmount(invoice.getTotalAmount().subtract(entry.getValue()));
                    invoiceRepository.save(invoice);
                });
            }
            
            // Delete old installments
            installmentRepository.deleteByBillId(billId);
        }

        // 4. Validate available limit (considering we're removing old installments)
        AvailableLimitResult limitResult = getAvailableLimit.execute(creditCardId);
        if (bill.getTotalAmount().compareTo(limitResult.getAvailableLimit()) > 0) {
            throw new CreditLimitExceededException(
                    String.format("Limite insuficiente. Necessário: %.2f, Disponível: %.2f",
                            bill.getTotalAmount(), limitResult.getAvailableLimit())
            );
        }

        // 5. Update Bill using existing UpdateBill use case
        bill.setId(billId);
        Bill updatedBill = updateBill.updateBill(billId, bill)
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        // 6. Cache for invoices to avoid duplicate updates
        Map<LocalDate, Invoice> invoiceCache = new HashMap<>();
        List<Installment> installments = new ArrayList<>();

        // 7. Process each installment
        for (int i = 1; i <= bill.getNumberOfInstallments(); i++) {
            // 7a. Calculate installment dates using domain service
            InstallmentDateInfo dateInfo = installmentDateCalculator.calculate(
                    bill.getExecutionDate(),
                    creditCard.getClosingDay(),
                    creditCard.getDueDay(),
                    i
            );

            // 7b. Get invoice from cache OR fetch/create from database
            Invoice invoice = invoiceCache.computeIfAbsent(
                    dateInfo.getReferenceMonth(),
                    refMonth -> generateOrGetInvoiceForMonth.execute(creditCardId, refMonth)
            );

            // 7c. Update invoice total amount (IN MEMORY)
            invoice.setTotalAmount(
                    invoice.getTotalAmount().add(updatedBill.getInstallmentAmount())
            );

            // 7d. Create installment entity (IN MEMORY)
            Installment installment = new Installment();
            installment.setBillId(updatedBill.getId());
            installment.setCreditCardId(creditCardId);
            installment.setInvoiceId(invoice.getId());
            installment.setInstallmentNumber(i);
            installment.setAmount(updatedBill.getInstallmentAmount());
            installment.setDueDate(dateInfo.getDueDate());
            installment.setCreatedAt(LocalDateTime.now());

            installments.add(installment);
        }

        // 8. BATCH SAVES for optimal performance
        // Save all modified invoices (only unique invoices, thanks to cache)
        invoiceRepository.saveAll(new ArrayList<>(invoiceCache.values()));

        // Save all installments in a single batch insert
        installmentRepository.saveAll(installments);

        // 9. Return the updated bill
        return updatedBill;
    }

    /**
     * Remove installments from a bill and update invoice totals.
     * Used when a bill is unlinked from a credit card.
     *
     * @param billId the ID of the bill
     * @param installments the installments to remove
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeInstallmentsAndUpdateInvoices(Long billId, List<Installment> installments) {
        if (installments == null || installments.isEmpty()) {
            return;
        }

        // Group installments by invoice to update invoice totals
        Map<Long, BigDecimal> invoiceAmountsToSubtract = new HashMap<>();
        for (Installment installment : installments) {
            invoiceAmountsToSubtract.merge(
                installment.getInvoiceId(),
                installment.getAmount(),
                BigDecimal::add
            );
        }

        // Update invoices by subtracting installment amounts
        for (Map.Entry<Long, BigDecimal> entry : invoiceAmountsToSubtract.entrySet()) {
            invoiceRepository.findById(entry.getKey()).ifPresent(invoice -> {
                invoice.setTotalAmount(invoice.getTotalAmount().subtract(entry.getValue()));
                invoiceRepository.save(invoice);
            });
        }

        // Delete installments
        installmentRepository.deleteByBillId(billId);
    }
}
