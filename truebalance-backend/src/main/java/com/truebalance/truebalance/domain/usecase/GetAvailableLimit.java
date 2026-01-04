package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.domain.entity.CreditCard;
import com.truebalance.truebalance.domain.entity.Invoice;
import com.truebalance.truebalance.domain.exception.CreditCardNotFoundException;
import com.truebalance.truebalance.domain.port.CreditCardRepositoryPort;
import com.truebalance.truebalance.domain.port.InstallmentRepositoryPort;
import com.truebalance.truebalance.domain.port.InvoiceRepositoryPort;
import com.truebalance.truebalance.domain.port.PartialPaymentRepositoryPort;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Use case to calculate the available credit limit for a credit card.
 *
 * Business Rule BR-CC-008:
 * availableLimit = creditLimit - usedLimit + partialPaymentsTotal
 *
 * Where:
 * - creditLimit: Fixed total limit of the card (from credit_cards table)
 * - usedLimit: Sum of all installments in OPEN and UNPAID invoices
 * - partialPaymentsTotal: Sum of all partial payments in OPEN and UNPAID invoices
 *
 * Only OPEN and UNPAID invoices (closed = false AND paid = false) are considered.
 * Paid invoices (paid = true) do not consume credit limit, freeing it up for new purchases.
 * The available limit can EXCEED the creditLimit if there are partial payments creating credit.
 *
 * Registered Available Limit:
 * If there's a CLOSED invoice with registerAvailableLimit = true:
 * - The most recent such invoice becomes the starting point for calculations
 * - All previous invoices are ignored
 * - The calculation starts from the registeredAvailableLimit value of that invoice
 * - Only invoices AFTER that reference month are considered in the calculation
 */
public class GetAvailableLimit {

    private final CreditCardRepositoryPort creditCardRepository;
    private final InvoiceRepositoryPort invoiceRepository;
    private final InstallmentRepositoryPort installmentRepository;
    private final PartialPaymentRepositoryPort partialPaymentRepository;

    public GetAvailableLimit(
            CreditCardRepositoryPort creditCardRepository,
            InvoiceRepositoryPort invoiceRepository,
            InstallmentRepositoryPort installmentRepository,
            PartialPaymentRepositoryPort partialPaymentRepository) {
        this.creditCardRepository = creditCardRepository;
        this.invoiceRepository = invoiceRepository;
        this.installmentRepository = installmentRepository;
        this.partialPaymentRepository = partialPaymentRepository;
    }

    /**
     * Calculate the available credit limit for a credit card.
     *
     * @param creditCardId the credit card ID
     * @return AvailableLimitResult with detailed limit information
     * @throws CreditCardNotFoundException if credit card not found
     */
    public AvailableLimitResult execute(Long creditCardId) {
        // 1. Fetch credit card (throws exception if not found)
        CreditCard creditCard = creditCardRepository.findById(creditCardId)
                .orElseThrow(() -> new CreditCardNotFoundException(creditCardId));

        BigDecimal creditLimit = creditCard.getCreditLimit();

        // 2. Check if there's a registered available limit
        List<Invoice> registeredLimitInvoices = invoiceRepository
                .findByCreditCardIdAndRegisterAvailableLimitOrderByReferenceMonthDesc(creditCardId, true);

        // 3. If there's a registered limit, use it as the starting point
        if (!registeredLimitInvoices.isEmpty()) {
            Invoice registeredLimitInvoice = registeredLimitInvoices.get(0); // Most recent
            return calculateFromRegisteredLimit(creditCardId, creditLimit, registeredLimitInvoice);
        }

        // 4. Otherwise, use the standard calculation
        return calculateStandardLimit(creditCardId, creditLimit);
    }

    /**
     * Calculate available limit starting from a registered limit invoice.
     * Ignores all invoices before the registered limit invoice.
     */
    private AvailableLimitResult calculateFromRegisteredLimit(
            Long creditCardId, BigDecimal creditLimit, Invoice registeredLimitInvoice) {

        BigDecimal startingLimit = registeredLimitInvoice.getRegisteredAvailableLimit();

        // Find all OPEN and UNPAID invoices AFTER the registered limit invoice
        List<Invoice> allOpenUnpaidInvoices = invoiceRepository
                .findByCreditCardIdAndClosedAndPaid(creditCardId, false, false);

        // Filter to keep only invoices after the registered limit invoice
        List<Invoice> relevantInvoices = allOpenUnpaidInvoices.stream()
                .filter(inv -> inv.getReferenceMonth().isAfter(registeredLimitInvoice.getReferenceMonth()))
                .collect(Collectors.toList());

        // If no relevant invoices, return the registered limit as-is
        if (relevantInvoices.isEmpty()) {
            return new AvailableLimitResult(
                    creditCardId,
                    creditLimit,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    startingLimit
            );
        }

        // Calculate used limit and partial payments from relevant invoices
        List<Long> relevantInvoiceIds = relevantInvoices.stream()
                .map(Invoice::getId)
                .collect(Collectors.toList());

        BigDecimal usedLimit = installmentRepository.sumAmountByInvoiceIds(relevantInvoiceIds);
        BigDecimal partialPaymentsTotal = partialPaymentRepository.sumAmountByInvoiceIds(relevantInvoiceIds);

        // Calculate available limit starting from registered limit
        // availableLimit = registeredLimit - usedLimit + partialPayments
        BigDecimal availableLimit = startingLimit
                .subtract(usedLimit)
                .add(partialPaymentsTotal);

        return new AvailableLimitResult(
                creditCardId,
                creditLimit,
                usedLimit,
                partialPaymentsTotal,
                availableLimit
        );
    }

    /**
     * Standard calculation when no registered limit is set.
     */
    private AvailableLimitResult calculateStandardLimit(Long creditCardId, BigDecimal creditLimit) {
        // Find all OPEN and UNPAID invoices for this credit card
        List<Invoice> openUnpaidInvoices = invoiceRepository
                .findByCreditCardIdAndClosedAndPaid(creditCardId, false, false);

        // If no open unpaid invoices, entire credit limit is available
        if (openUnpaidInvoices.isEmpty()) {
            return new AvailableLimitResult(
                    creditCardId,
                    creditLimit,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    creditLimit
            );
        }

        // Extract invoice IDs
        List<Long> openUnpaidInvoiceIds = openUnpaidInvoices.stream()
                .map(Invoice::getId)
                .collect(Collectors.toList());

        // Sum all installment amounts from open unpaid invoices
        BigDecimal usedLimit = installmentRepository.sumAmountByInvoiceIds(openUnpaidInvoiceIds);

        // Sum all partial payment amounts from open unpaid invoices
        BigDecimal partialPaymentsTotal = partialPaymentRepository.sumAmountByInvoiceIds(openUnpaidInvoiceIds);

        // Calculate available limit (BR-CC-008)
        // availableLimit = creditLimit - usedLimit + partialPayments
        BigDecimal availableLimit = creditLimit
                .subtract(usedLimit)
                .add(partialPaymentsTotal);

        return new AvailableLimitResult(
                creditCardId,
                creditLimit,
                usedLimit,
                partialPaymentsTotal,
                availableLimit
        );
    }
}
