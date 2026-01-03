package com.truebalance.truebalance.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain entity representing a partial payment made on an invoice.
 * Partial payments reduce the final amount to be paid when the invoice is closed.
 * BR-PP-001: Can only be registered on open invoices with allowsPartialPayment = true
 * BR-PP-002: Amount can exceed invoice balance (creates negative balance / credit)
 * BR-PP-004: Immutable - cannot be edited, only created or deleted
 */
public class PartialPayment {

    private Long id;
    private Long invoiceId;             // Reference to invoice
    private BigDecimal amount;          // Payment amount (must be > 0)
    private LocalDateTime paymentDate;  // When the payment was made
    private String description;         // Optional description/note
    private LocalDateTime createdAt;    // Creation timestamp

    public PartialPayment() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
