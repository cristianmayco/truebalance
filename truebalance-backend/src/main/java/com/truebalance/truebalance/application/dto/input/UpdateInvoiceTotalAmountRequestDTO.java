package com.truebalance.truebalance.application.dto.input;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO para atualizar o valor total de uma fatura.
 * Apenas permitido quando useAbsoluteValue = true.
 */
public class UpdateInvoiceTotalAmountRequestDTO {

    @NotNull(message = "O valor total é obrigatório")
    @DecimalMin(value = "0.0", inclusive = true, message = "O valor total deve ser maior ou igual a zero")
    private BigDecimal totalAmount;

    public UpdateInvoiceTotalAmountRequestDTO() {
    }

    public UpdateInvoiceTotalAmountRequestDTO(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
