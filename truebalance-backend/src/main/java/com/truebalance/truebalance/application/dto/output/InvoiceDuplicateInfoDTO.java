package com.truebalance.truebalance.application.dto.output;

import java.math.BigDecimal;
import java.time.LocalDate;

public class InvoiceDuplicateInfoDTO {

    private Integer lineNumber;
    private Long creditCardId;
    private LocalDate referenceMonth;
    private BigDecimal totalAmount;
    private Long existingInvoiceId;
    private String reason;

    public InvoiceDuplicateInfoDTO() {
    }

    public InvoiceDuplicateInfoDTO(Integer lineNumber, Long creditCardId, LocalDate referenceMonth,
                                   BigDecimal totalAmount, Long existingInvoiceId, String reason) {
        this.lineNumber = lineNumber;
        this.creditCardId = creditCardId;
        this.referenceMonth = referenceMonth;
        this.totalAmount = totalAmount;
        this.existingInvoiceId = existingInvoiceId;
        this.reason = reason;
    }

    // Getters and Setters
    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
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

    public Long getExistingInvoiceId() {
        return existingInvoiceId;
    }

    public void setExistingInvoiceId(Long existingInvoiceId) {
        this.existingInvoiceId = existingInvoiceId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
