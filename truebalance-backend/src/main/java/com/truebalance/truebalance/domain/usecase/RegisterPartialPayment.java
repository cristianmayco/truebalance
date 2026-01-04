package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.domain.entity.CreditCard;
import com.truebalance.truebalance.domain.entity.Invoice;
import com.truebalance.truebalance.domain.entity.PartialPayment;
import com.truebalance.truebalance.domain.exception.InvoiceClosedException;
import com.truebalance.truebalance.domain.exception.InvoiceNotFoundException;
import com.truebalance.truebalance.domain.exception.PartialPaymentNotAllowedException;
import com.truebalance.truebalance.domain.port.CreditCardRepositoryPort;
import com.truebalance.truebalance.domain.port.InvoiceRepositoryPort;
import com.truebalance.truebalance.domain.port.PartialPaymentRepositoryPort;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Use case to register a partial payment on an open invoice.
 * BR-PP-001: Validates invoice is open and credit card allows partial payments
 * BR-PP-002: Amount can exceed invoice balance (creates credit)
 * BR-PP-006: Calculates and returns available limit in real-time
 *
 * This is a COMPLEX use case with multiple validations.
 */
public class RegisterPartialPayment {

    private final PartialPaymentRepositoryPort partialPaymentRepository;
    private final InvoiceRepositoryPort invoiceRepository;
    private final CreditCardRepositoryPort creditCardRepository;
    private final GetAvailableLimit getAvailableLimit;

    public RegisterPartialPayment(PartialPaymentRepositoryPort partialPaymentRepository,
                                   InvoiceRepositoryPort invoiceRepository,
                                   CreditCardRepositoryPort creditCardRepository,
                                   GetAvailableLimit getAvailableLimit) {
        this.partialPaymentRepository = partialPaymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.creditCardRepository = creditCardRepository;
        this.getAvailableLimit = getAvailableLimit;
    }

    /**
     * Execute the use case.
     * Returns a wrapper object with the partial payment and calculated available limit.
     *
     * @param invoiceId the ID of the invoice
     * @param partialPayment the partial payment to register (amount and description)
     * @return RegisterPartialPaymentResult with payment and available limit
     * @throws IllegalStateException if validation fails
     */
    public RegisterPartialPaymentResult execute(Long invoiceId, PartialPayment partialPayment) {
        // Step 1: Verify invoice exists
        Optional<Invoice> invoiceOpt = invoiceRepository.findById(invoiceId);

        if (invoiceOpt.isEmpty()) {
            throw new InvoiceNotFoundException(invoiceId);
        }

        Invoice invoice = invoiceOpt.get();

        // Step 2: Fetch credit card associated with the invoice
        Optional<CreditCard> creditCardOpt = creditCardRepository.findById(invoice.getCreditCardId());

        if (creditCardOpt.isEmpty()) {
            throw new IllegalStateException("Credit card not found for invoice ID: " + invoiceId);
        }

        CreditCard creditCard = creditCardOpt.get();

        // Step 3: BR-PP-001: Validate credit card allows partial payments
        if (!creditCard.isAllowsPartialPayment()) {
            throw new PartialPaymentNotAllowedException(creditCard.getId());
        }

        // Step 4: BR-PP-001: Validate invoice is open
        if (invoice.isClosed()) {
            throw new InvoiceClosedException(invoiceId);
        }

        // Step 5: Validate amount > 0
        if (partialPayment.getAmount() == null || partialPayment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Valor do pagamento deve ser maior que zero");
        }

        // Step 6: BR-PP-002: Amount CAN exceed invoice balance (no validation against balance)

        // Step 7: Set invoice ID and payment date
        partialPayment.setInvoiceId(invoiceId);
        partialPayment.setPaymentDate(LocalDateTime.now());

        // Step 8: Save the partial payment
        PartialPayment saved = partialPaymentRepository.save(partialPayment);

        // Step 9: BR-PP-006: Calculate available limit
        // Phase 4.5: Full implementation that considers installments and partial payments
        // availableLimit = creditLimit - SUM(installments of open invoices) + SUM(partial_payments of open invoices)
        AvailableLimitResult limitResult = getAvailableLimit.execute(creditCard.getId());
        BigDecimal availableLimit = limitResult.getAvailableLimit();

        // Step 10: Return result with payment and available limit
        return new RegisterPartialPaymentResult(saved, availableLimit);
    }

    /**
     * Result wrapper for RegisterPartialPayment use case.
     * Contains the saved partial payment and the calculated available limit.
     */
    public static class RegisterPartialPaymentResult {
        private final PartialPayment partialPayment;
        private final BigDecimal availableLimit;

        public RegisterPartialPaymentResult(PartialPayment partialPayment, BigDecimal availableLimit) {
            this.partialPayment = partialPayment;
            this.availableLimit = availableLimit;
        }

        public PartialPayment getPartialPayment() {
            return partialPayment;
        }

        public BigDecimal getAvailableLimit() {
            return availableLimit;
        }
    }
}
