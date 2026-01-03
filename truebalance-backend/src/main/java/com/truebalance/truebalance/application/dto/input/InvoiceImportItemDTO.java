package com.truebalance.truebalance.application.dto.input;

import com.truebalance.truebalance.domain.entity.Invoice;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class InvoiceImportItemDTO {

    @NotNull(message = "ID do cartão de crédito é obrigatório")
    private Long creditCardId;

    @NotNull(message = "Mês de referência é obrigatório")
    private LocalDate referenceMonth;

    @NotNull(message = "Valor total é obrigatório")
    @PositiveOrZero(message = "Valor total deve ser positivo ou zero")
    private BigDecimal totalAmount;

    @PositiveOrZero(message = "Saldo anterior deve ser positivo ou zero")
    private BigDecimal previousBalance;

    private Boolean closed;

    private Boolean paid;

    @NotNull(message = "Número da linha é obrigatório")
    private Integer lineNumber;

    public InvoiceImportItemDTO() {
    }

    public InvoiceImportItemDTO(Long creditCardId, LocalDate referenceMonth,
                                BigDecimal totalAmount, BigDecimal previousBalance,
                                Boolean closed, Boolean paid, Integer lineNumber) {
        this.creditCardId = creditCardId;
        this.referenceMonth = referenceMonth;
        this.totalAmount = totalAmount;
        this.previousBalance = previousBalance;
        this.closed = closed;
        this.paid = paid;
        this.lineNumber = lineNumber;
    }

    public Invoice toInvoice() {
        Invoice invoice = new Invoice();
        invoice.setCreditCardId(this.creditCardId);
        invoice.setReferenceMonth(this.referenceMonth);
        invoice.setTotalAmount(this.totalAmount);
        invoice.setPreviousBalance(this.previousBalance != null ? this.previousBalance : BigDecimal.ZERO);
        invoice.setClosed(this.closed != null ? this.closed : false);
        invoice.setPaid(this.paid != null ? this.paid : false);
        return invoice;
    }

    // Getters and Setters
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

    public Boolean getClosed() {
        return closed;
    }

    public void setClosed(Boolean closed) {
        this.closed = closed;
    }

    public Boolean getPaid() {
        return paid;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }
}
