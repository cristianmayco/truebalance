package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.domain.entity.*;
import com.truebalance.truebalance.domain.exception.CreditCardNotFoundException;
import com.truebalance.truebalance.domain.exception.CreditLimitExceededException;
import com.truebalance.truebalance.domain.port.CreditCardRepositoryPort;
import com.truebalance.truebalance.domain.port.InstallmentRepositoryPort;
import com.truebalance.truebalance.domain.port.InvoiceRepositoryPort;
import com.truebalance.truebalance.domain.service.InstallmentDateCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Use case to create a bill linked to a credit card with automatic installment distribution.
 *
 * This is the CORE integration between Bills and CreditCards (Phase 4.5).
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
 * If any step fails, ALL changes are rolled back (Bill, Installments, Invoices).
 */
public class CreateBillWithCreditCard {

    private static final Logger logger = LoggerFactory.getLogger(CreateBillWithCreditCard.class);
    
    private final CreateBill createBill;
    private final CreditCardRepositoryPort creditCardRepository;
    private final InstallmentRepositoryPort installmentRepository;
    private final InvoiceRepositoryPort invoiceRepository;
    private final GenerateOrGetInvoiceForMonth generateOrGetInvoiceForMonth;
    private final GetAvailableLimit getAvailableLimit;
    private final InstallmentDateCalculator installmentDateCalculator;

    public CreateBillWithCreditCard(
            CreateBill createBill,
            CreditCardRepositoryPort creditCardRepository,
            InstallmentRepositoryPort installmentRepository,
            InvoiceRepositoryPort invoiceRepository,
            GenerateOrGetInvoiceForMonth generateOrGetInvoiceForMonth,
            GetAvailableLimit getAvailableLimit,
            InstallmentDateCalculator installmentDateCalculator) {
        this.createBill = createBill;
        this.creditCardRepository = creditCardRepository;
        this.installmentRepository = installmentRepository;
        this.invoiceRepository = invoiceRepository;
        this.generateOrGetInvoiceForMonth = generateOrGetInvoiceForMonth;
        this.getAvailableLimit = getAvailableLimit;
        this.installmentDateCalculator = installmentDateCalculator;
    }

    /**
     * Execute the creation of a bill with credit card integration.
     *
     * @param bill         the bill to create
     * @param creditCardId the credit card ID to link to
     * @return the created bill
     * @throws CreditCardNotFoundException   if credit card not found
     * @throws CreditLimitExceededException if available limit is insufficient
     */
    @Transactional(rollbackFor = Exception.class)
    public Bill execute(Bill bill, Long creditCardId) {
        // 1. Validate credit card exists
        CreditCard creditCard = creditCardRepository.findById(creditCardId)
                .orElseThrow(() -> new CreditCardNotFoundException(creditCardId));

        // 2. Validate available limit ONCE at the beginning (BR-CC-008)
        AvailableLimitResult limitResult = getAvailableLimit.execute(creditCardId);
        if (bill.getTotalAmount().compareTo(limitResult.getAvailableLimit()) > 0) {
            throw new CreditLimitExceededException(
                    String.format("Limite insuficiente. Necessário: %.2f, Disponível: %.2f",
                            bill.getTotalAmount(), limitResult.getAvailableLimit())
            );
        }

        // 3. Create Bill using existing CreateBill use case (COMPOSITION)
        Bill savedBill = createBill.addBill(bill);

        // 4. Cache for invoices to avoid duplicate updates (PERFORMANCE OPTIMIZATION)
        Map<LocalDate, Invoice> invoiceCache = new HashMap<>();
        List<Installment> installments = new ArrayList<>();

        // 5. Process each installment
        logger.info("Processing {} installments for bill. Execution date: {}, Closing day: {}, Due day: {}", 
                bill.getNumberOfInstallments(), bill.getExecutionDate(), creditCard.getClosingDay(), creditCard.getDueDay());
        
        for (int i = 1; i <= bill.getNumberOfInstallments(); i++) {
            // 5a. Calculate installment dates using domain service
            InstallmentDateInfo dateInfo = installmentDateCalculator.calculate(
                    bill.getExecutionDate(),
                    creditCard.getClosingDay(),
                    creditCard.getDueDay(),
                    i
            );

            logger.info("Installment {}: Due date: {}, Reference month: {}", 
                    i, dateInfo.getDueDate(), dateInfo.getReferenceMonth());

            // 5b. Get invoice from cache OR fetch/create from database
            // This ensures we only update each invoice ONCE, even if multiple installments belong to it
            Invoice invoice = invoiceCache.computeIfAbsent(
                    dateInfo.getReferenceMonth(),
                    refMonth -> generateOrGetInvoiceForMonth.execute(creditCardId, refMonth)
            );
            
            logger.info("Installment {} assigned to invoice ID: {}, Reference month: {}", 
                    i, invoice.getId(), invoice.getReferenceMonth());

            // 5c. Update invoice total amount (IN MEMORY)
            // Only update if invoice doesn't use absolute value (BR-I-018)
            if (!invoice.isUseAbsoluteValue()) {
                invoice.setTotalAmount(
                        invoice.getTotalAmount().add(savedBill.getInstallmentAmount())
                );
            } else {
                logger.debug("Invoice ID={} uses absolute value, skipping totalAmount update", invoice.getId());
            }

            // 5d. Create installment entity (IN MEMORY)
            Installment installment = new Installment();
            installment.setBillId(savedBill.getId());
            installment.setCreditCardId(creditCardId);
            installment.setInvoiceId(invoice.getId());
            installment.setInstallmentNumber(i);
            installment.setAmount(savedBill.getInstallmentAmount());
            installment.setDueDate(dateInfo.getDueDate());
            installment.setCreatedAt(LocalDateTime.now());

            installments.add(installment);
        }

        // 6. BATCH SAVES for optimal performance
        // Save all modified invoices (only unique invoices, thanks to cache)
        invoiceRepository.saveAll(new ArrayList<>(invoiceCache.values()));

        // Save all installments in a single batch insert
        installmentRepository.saveAll(installments);

        // 7. Return the created bill
        return savedBill;
    }
}
