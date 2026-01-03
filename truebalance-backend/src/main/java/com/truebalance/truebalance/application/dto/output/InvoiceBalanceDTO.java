package com.truebalance.truebalance.application.dto.output;

import java.math.BigDecimal;

public class InvoiceBalanceDTO {

    private Long invoiceId;
    private BigDecimal totalAmount;
    private BigDecimal previousBalance;
    private BigDecimal partialPaymentsTotal;
    private BigDecimal currentBalance;
    private boolean paid;
    private boolean closed;
    private int partialPaymentsCount;

    public InvoiceBalanceDTO() {
    }

    public InvoiceBalanceDTO(Long invoiceId, BigDecimal totalAmount, BigDecimal previousBalance,
                             BigDecimal partialPaymentsTotal, BigDecimal currentBalance,
                             boolean paid, boolean closed, int partialPaymentsCount) {
        this.invoiceId = invoiceId;
        this.totalAmount = totalAmount;
        this.previousBalance = previousBalance;
        this.partialPaymentsTotal = partialPaymentsTotal;
        this.currentBalance = currentBalance;
        this.paid = paid;
        this.closed = closed;
        this.partialPaymentsCount = partialPaymentsCount;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
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

    public BigDecimal getPartialPaymentsTotal() {
        return partialPaymentsTotal;
    }

    public void setPartialPaymentsTotal(BigDecimal partialPaymentsTotal) {
        this.partialPaymentsTotal = partialPaymentsTotal;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public int getPartialPaymentsCount() {
        return partialPaymentsCount;
    }

    public void setPartialPaymentsCount(int partialPaymentsCount) {
        this.partialPaymentsCount = partialPaymentsCount;
    }
}
