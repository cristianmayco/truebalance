package com.truebalance.truebalance.application.dto.output;

import com.truebalance.truebalance.domain.entity.Invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class InvoiceResponseDTO {

    private Long id;
    private Long creditCardId;
    private LocalDate referenceMonth;
    private BigDecimal totalAmount;
    private BigDecimal previousBalance;
    private boolean closed;
    private boolean paid;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public InvoiceResponseDTO() {
    }

    public InvoiceResponseDTO(Long id, Long creditCardId, LocalDate referenceMonth,
                              BigDecimal totalAmount, BigDecimal previousBalance,
                              boolean closed, boolean paid,
                              LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.creditCardId = creditCardId;
        this.referenceMonth = referenceMonth;
        this.totalAmount = totalAmount;
        this.previousBalance = previousBalance;
        this.closed = closed;
        this.paid = paid;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static InvoiceResponseDTO fromInvoice(Invoice invoice) {
        return new InvoiceResponseDTO(
                invoice.getId(),
                invoice.getCreditCardId(),
                invoice.getReferenceMonth(),
                invoice.getTotalAmount(),
                invoice.getPreviousBalance(),
                invoice.isClosed(),
                invoice.isPaid(),
                invoice.getCreatedAt(),
                invoice.getUpdatedAt()
        );
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
}
