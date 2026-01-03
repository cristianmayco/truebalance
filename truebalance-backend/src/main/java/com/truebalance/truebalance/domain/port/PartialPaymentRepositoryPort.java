package com.truebalance.truebalance.domain.port;

import com.truebalance.truebalance.domain.entity.PartialPayment;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository port for PartialPayment entity.
 * Implemented by PartialPaymentRepositoryAdapter in the infrastructure layer.
 */
public interface PartialPaymentRepositoryPort {

    /**
     * Save a partial payment.
     * @param partialPayment partial payment to save
     * @return saved partial payment with generated ID
     */
    PartialPayment save(PartialPayment partialPayment);

    /**
     * Find a partial payment by ID.
     * @param id partial payment ID
     * @return Optional containing the partial payment if found
     */
    Optional<PartialPayment> findById(Long id);

    /**
     * Find all partial payments for an invoice, ordered by payment date descending.
     * @param invoiceId invoice ID
     * @return list of partial payments
     */
    List<PartialPayment> findByInvoiceId(Long invoiceId);

    /**
     * Calculate sum of all partial payments for an invoice.
     * @param invoiceId Invoice ID
     * @return Sum of partial payments
     */
    BigDecimal sumByInvoiceId(Long invoiceId);

    /**
     * Count number of partial payments for an invoice.
     * @param invoiceId Invoice ID
     * @return Count of partial payments
     */
    int countByInvoiceId(Long invoiceId);

    /**
     * Sum the amounts of all partial payments belonging to the specified invoices.
     * Used for calculating available credit limit.
     *
     * @param invoiceIds list of invoice IDs
     * @return the sum of partial payment amounts, or zero if no payments found
     */
    BigDecimal sumAmountByInvoiceIds(List<Long> invoiceIds);

    /**
     * Delete a partial payment by ID.
     * @param id partial payment ID
     */
    void deleteById(Long id);
}
