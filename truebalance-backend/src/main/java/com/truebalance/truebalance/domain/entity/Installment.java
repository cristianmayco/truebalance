package com.truebalance.truebalance.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Domain entity representing a single installment of a bill.
 * An installment represents one payment portion of a bill, linked to a specific invoice.
 */
public class Installment {

    private Long id;
    private Long billId;                // Reference to parent bill
    private Long creditCardId;          // Reference to credit card (nullable)
    private Long invoiceId;             // Reference to invoice (nullable)
    private int installmentNumber;      // Installment number (e.g., 1 of 4)
    private BigDecimal amount;          // Amount of this installment
    private LocalDate dueDate;          // When this installment is due
    private LocalDateTime createdAt;    // Creation timestamp

    public Installment() {
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
