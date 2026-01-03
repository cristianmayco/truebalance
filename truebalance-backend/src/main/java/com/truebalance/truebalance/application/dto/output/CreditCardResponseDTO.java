package com.truebalance.truebalance.application.dto.output;

import com.truebalance.truebalance.domain.entity.CreditCard;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CreditCardResponseDTO {

    private Long id;
    private String name;
    private BigDecimal creditLimit;
    private int closingDay;
    private int dueDay;
    private boolean allowsPartialPayment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CreditCardResponseDTO() {
    }

    public CreditCardResponseDTO(Long id, String name, BigDecimal creditLimit, int closingDay,
                                  int dueDay, boolean allowsPartialPayment,
                                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.creditLimit = creditLimit;
        this.closingDay = closingDay;
        this.dueDay = dueDay;
        this.allowsPartialPayment = allowsPartialPayment;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CreditCardResponseDTO fromCreditCard(CreditCard creditCard) {
        return new CreditCardResponseDTO(
                creditCard.getId(),
                creditCard.getName(),
                creditCard.getCreditLimit(),
                creditCard.getClosingDay(),
                creditCard.getDueDay(),
                creditCard.isAllowsPartialPayment(),
                creditCard.getCreatedAt(),
                creditCard.getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public int getClosingDay() {
        return closingDay;
    }

    public void setClosingDay(int closingDay) {
        this.closingDay = closingDay;
    }

    public int getDueDay() {
        return dueDay;
    }

    public void setDueDay(int dueDay) {
        this.dueDay = dueDay;
    }

    public boolean isAllowsPartialPayment() {
        return allowsPartialPayment;
    }

    public void setAllowsPartialPayment(boolean allowsPartialPayment) {
        this.allowsPartialPayment = allowsPartialPayment;
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
