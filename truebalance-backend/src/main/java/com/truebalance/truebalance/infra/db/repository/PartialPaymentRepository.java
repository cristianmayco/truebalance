package com.truebalance.truebalance.infra.db.repository;

import com.truebalance.truebalance.infra.db.entity.PartialPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Spring Data JPA repository for PartialPayment entities.
 */
@Repository
public interface PartialPaymentRepository extends JpaRepository<PartialPaymentEntity, Long> {

    /**
     * Find all partial payments for a specific invoice, ordered by payment date.
     *
     * @param invoiceId the invoice ID
     * @return list of partial payments ordered by payment date descending (most recent first)
     */
    List<PartialPaymentEntity> findByInvoiceIdOrderByPaymentDateDesc(Long invoiceId);

    /**
     * Calculate the sum of all partial payments for a specific invoice.
     *
     * @param invoiceId the invoice ID
     * @return total sum of partial payments, or 0.00 if no payments exist
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PartialPaymentEntity p WHERE p.invoiceId = :invoiceId")
    BigDecimal sumByInvoiceId(@Param("invoiceId") Long invoiceId);

    /**
     * Count the number of partial payments for a specific invoice.
     *
     * @param invoiceId the invoice ID
     * @return count of partial payments
     */
    int countByInvoiceId(Long invoiceId);

    /**
     * Sum the amounts of all partial payments belonging to the specified invoices.
     *
     * @param invoiceIds list of invoice IDs
     * @return the sum of partial payment amounts, or zero if no payments found
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PartialPaymentEntity p WHERE p.invoiceId IN :invoiceIds")
    BigDecimal sumAmountByInvoiceIds(@Param("invoiceIds") List<Long> invoiceIds);
}
