package com.truebalance.truebalance.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Invoice {

    private Long id;
    private Long creditCardId;
    private LocalDate referenceMonth;
    private BigDecimal totalAmount;
    private BigDecimal previousBalance;
    private boolean closed;
    private boolean paid;
    private boolean useAbsoluteValue; // Se true, totalAmount não é recalculado pela soma das parcelas
    private boolean registerAvailableLimit; // Se true, esta fatura é o ponto de partida para cálculos de limite
    private BigDecimal registeredAvailableLimit; // Valor do limite disponível registrado (usado quando registerAvailableLimit = true)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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

    public boolean isUseAbsoluteValue() {
        return useAbsoluteValue;
    }

    public void setUseAbsoluteValue(boolean useAbsoluteValue) {
        this.useAbsoluteValue = useAbsoluteValue;
    }

    public boolean isRegisterAvailableLimit() {
        return registerAvailableLimit;
    }

    public void setRegisterAvailableLimit(boolean registerAvailableLimit) {
        this.registerAvailableLimit = registerAvailableLimit;
    }

    public BigDecimal getRegisteredAvailableLimit() {
        return registeredAvailableLimit;
    }

    public void setRegisteredAvailableLimit(BigDecimal registeredAvailableLimit) {
        this.registeredAvailableLimit = registeredAvailableLimit;
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
