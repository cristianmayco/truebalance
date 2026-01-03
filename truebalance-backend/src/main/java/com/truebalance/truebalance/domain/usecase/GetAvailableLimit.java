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
 * - usedLimit: Sum of all installments in OPEN invoices
 * - partialPaymentsTotal: Sum of all partial payments in OPEN invoices
 *
 * Only OPEN invoices (closed = false) are considered.
 * The available limit can EXCEED the creditLimit if there are partial payments creating credit.
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

        // 2. Find all OPEN invoices for this credit card
        List<Invoice> openInvoices = invoiceRepository
                .findByCreditCardIdAndClosed(creditCardId, false);

        // 3. If no open invoices, entire credit limit is available
        if (openInvoices.isEmpty()) {
            return new AvailableLimitResult(
                    creditCardId,
                    creditLimit,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    creditLimit
            );
        }

        // 4. Extract invoice IDs
        List<Long> openInvoiceIds = openInvoices.stream()
                .map(Invoice::getId)
                .collect(Collectors.toList());

        // 5. Sum all installment amounts from open invoices (BATCH QUERY)
        BigDecimal usedLimit = installmentRepository.sumAmountByInvoiceIds(openInvoiceIds);

        // 6. Sum all partial payment amounts from open invoices (BATCH QUERY)
        BigDecimal partialPaymentsTotal = partialPaymentRepository.sumAmountByInvoiceIds(openInvoiceIds);

        // 7. Calculate available limit (BR-CC-008)
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
