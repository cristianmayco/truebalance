package com.truebalance.truebalance.infra.db.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA entity for installments.
 * Represents a single payment portion of a bill, linked to an invoice.
 */
@Entity
@Table(name = "installments",
       indexes = {
           @Index(name = "idx_installment_bill", columnList = "bill_id"),
           @Index(name = "idx_installment_invoice", columnList = "invoice_id"),
           @Index(name = "idx_installment_credit_card", columnList = "credit_card_id"),
           @Index(name = "idx_installment_due_date", columnList = "due_date")
       })
public class InstallmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bill_id", nullable = false)
    private Long billId;

    @Column(name = "credit_card_id")
    private Long creditCardId;

    @Column(name = "invoice_id")
    private Long invoiceId;

    @Column(name = "installment_number", nullable = false)
    private int installmentNumber;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public InstallmentEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public Long getCreditCardId() {
        return creditCardId;
    }

    public void setCreditCardId(Long creditCardId) {
        this.creditCardId = creditCardId;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public int getInstallmentNumber() {
        return installmentNumber;
    }

    public void setInstallmentNumber(int installmentNumber) {
        this.installmentNumber = installmentNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
