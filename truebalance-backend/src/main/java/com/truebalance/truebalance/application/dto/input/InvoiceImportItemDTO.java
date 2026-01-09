package com.truebalance.truebalance.application.dto.input;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InvoiceImportItemDTO {
    @NotNull(message = "ID do cartão de crédito é obrigatório")
    private Long creditCardId;

    @NotNull(message = "Mês de referência é obrigatório")
    private LocalDate referenceMonth;

    @NotNull(message = "Valor total é obrigatório")
    @PositiveOrZero(message = "Valor total deve ser positivo ou zero")
    private BigDecimal totalAmount;

    private BigDecimal previousBalance;

    private Boolean closed;

    private Boolean paid;

    private Boolean useAbsoluteValue; // Se true, totalAmount não é recalculado pela soma das parcelas

    public InvoiceImportItemDTO() {
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

    public Boolean getUseAbsoluteValue() {
        return useAbsoluteValue;
    }

    public void setUseAbsoluteValue(Boolean useAbsoluteValue) {
        this.useAbsoluteValue = useAbsoluteValue;
    }
}
