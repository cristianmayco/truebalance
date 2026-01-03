package com.truebalance.truebalance.domain.port;

import com.truebalance.truebalance.domain.entity.Installment;

import java.math.BigDecimal;
import java.util.List;

/**
 * Port interface for Installment repository operations.
 * Defines the contract for installment persistence.
 */
public interface InstallmentRepositoryPort {

    /**
     * Save a single installment.
     *
     * @param installment the installment to save
     * @return the saved installment
     */
    Installment save(Installment installment);

    /**
     * Save multiple installments at once.
     *
     * @param installments the list of installments to save
     * @return the list of saved installments
     */
    List<Installment> saveAll(List<Installment> installments);

    /**
     * Find all installments belonging to a specific bill.
     *
     * @param billId the bill ID
     * @return list of installments ordered by installment number
     */
    List<Installment> findByBillId(Long billId);

    /**
     * Find all installments belonging to a specific invoice.
     *
     * @param invoiceId the invoice ID
     * @return list of installments ordered by due date
     */
    List<Installment> findByInvoiceId(Long invoiceId);

    /**
     * Delete all installments belonging to a specific bill.
     * Used when a bill is deleted (cascade delete).
     *
     * @param billId the bill ID
     */
    void deleteByBillId(Long billId);

    /**
     * Sum the amounts of all installments belonging to the specified invoices.
     * Used for calculating available credit limit.
     *
     * @param invoiceIds list of invoice IDs
     * @return the sum of installment amounts, or zero if no installments found
     */
    BigDecimal sumAmountByInvoiceIds(List<Long> invoiceIds);
}
