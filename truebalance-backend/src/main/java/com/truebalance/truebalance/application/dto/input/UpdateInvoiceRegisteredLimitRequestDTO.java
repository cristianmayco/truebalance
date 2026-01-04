package com.truebalance.truebalance.application.dto.input;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO para atualizar o limite disponível registrado em uma fatura.
 * Apenas permitido para faturas fechadas.
 */
public class UpdateInvoiceRegisteredLimitRequestDTO {

    @NotNull(message = "registerAvailableLimit é obrigatório")
    private Boolean registerAvailableLimit;

    @DecimalMin(value = "0.0", inclusive = true, message = "O limite registrado deve ser maior ou igual a zero")
    private BigDecimal registeredAvailableLimit;

    public UpdateInvoiceRegisteredLimitRequestDTO() {
    }

    public UpdateInvoiceRegisteredLimitRequestDTO(Boolean registerAvailableLimit, BigDecimal registeredAvailableLimit) {
        this.registerAvailableLimit = registerAvailableLimit;
        this.registeredAvailableLimit = registeredAvailableLimit;
    }

    public Boolean getRegisterAvailableLimit() {
        return registerAvailableLimit;
    }

    public void setRegisterAvailableLimit(Boolean registerAvailableLimit) {
        this.registerAvailableLimit = registerAvailableLimit;
    }

    public BigDecimal getRegisteredAvailableLimit() {
        return registeredAvailableLimit;
    }

    public void setRegisteredAvailableLimit(BigDecimal registeredAvailableLimit) {
        this.registeredAvailableLimit = registeredAvailableLimit;
    }
}
