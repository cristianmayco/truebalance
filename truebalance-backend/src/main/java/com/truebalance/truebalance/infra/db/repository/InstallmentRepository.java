package com.truebalance.truebalance.infra.db.repository;

import com.truebalance.truebalance.infra.db.entity.InstallmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Spring Data JPA repository for Installment entities.
 */
@Repository
public interface InstallmentRepository extends JpaRepository<InstallmentEntity, Long> {

    /**
     * Find all installments for a specific bill, ordered by installment number.
     *
     * @param billId the bill ID
     * @return list of installments ordered by installment number ascending
     */
    List<InstallmentEntity> findByBillIdOrderByInstallmentNumberAsc(Long billId);

    /**
     * Find all installments for a specific invoice, ordered by due date.
     *
     * @param invoiceId the invoice ID
     * @return list of installments ordered by due date ascending
     */
    List<InstallmentEntity> findByInvoiceIdOrderByDueDateAsc(Long invoiceId);

    /**
     * Delete all installments belonging to a specific bill.
     * Used for cascade delete when bill is deleted.
     *
     * @param billId the bill ID
     */
    void deleteByBillId(Long billId);

    /**
     * Sum the amounts of all installments belonging to the specified invoices.
     *
     * @param invoiceIds list of invoice IDs
     * @return the sum of installment amounts, or zero if no installments found
     */
    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM InstallmentEntity i WHERE i.invoiceId IN :invoiceIds")
    BigDecimal sumAmountByInvoiceIds(@Param("invoiceIds") List<Long> invoiceIds);
}
