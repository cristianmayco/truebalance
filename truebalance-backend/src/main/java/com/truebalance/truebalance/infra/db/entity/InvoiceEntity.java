package com.truebalance.truebalance.infra.db.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_invoice_card_month",
           columnNames = {"credit_card_id", "reference_month"}
       ),
       indexes = @Index(
           name = "idx_invoice_card_month",
           columnList = "credit_card_id, reference_month"
       ))
public class InvoiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "credit_card_id", nullable = false)
    private Long creditCardId;

    @Column(name = "reference_month", nullable = false)
    private LocalDate referenceMonth;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "previous_balance", nullable = false, precision = 10, scale = 2)
    private BigDecimal previousBalance;

    @Column(nullable = false)
    private boolean closed;

    @Column(nullable = false)
    private boolean paid;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public InvoiceEntity() {
    }

    public InvoiceEntity(Long creditCardId, LocalDate referenceMonth, BigDecimal totalAmount,
                         BigDecimal previousBalance, boolean closed, boolean paid) {
        this.creditCardId = creditCardId;
        this.referenceMonth = referenceMonth;
        this.totalAmount = totalAmount;
        this.previousBalance = previousBalance;
        this.closed = closed;
        this.paid = paid;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCreditCardId() {
        return creditCardId;
    }

    public void setCreditCardId(Long creditCardId) {
        this.creditCardId = creditCardId;
    }

    public LocalDate getReferenceMonth() {
        return referenceMonth;
    }

    public void setReferenceMonth(LocalDate referenceMonth) {
        this.referenceMonth = referenceMonth;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getPreviousBalance() {
        return previousBalance;
    }

    public void setPreviousBalance(BigDecimal previousBalance) {
        this.previousBalance = previousBalance;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
