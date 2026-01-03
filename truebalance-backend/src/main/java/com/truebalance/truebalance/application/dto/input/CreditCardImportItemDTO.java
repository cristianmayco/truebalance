package com.truebalance.truebalance.application.dto.input;

import com.truebalance.truebalance.domain.entity.CreditCard;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class CreditCardImportItemDTO {

    @NotBlank(message = "Nome do cartão é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    private String name;

    @NotNull(message = "Limite de crédito é obrigatório")
    @Positive(message = "Limite de crédito deve ser positivo")
    private BigDecimal creditLimit;

    @NotNull(message = "Dia de fechamento é obrigatório")
    @Min(value = 1, message = "Dia de fechamento deve ser entre 1 e 31")
    @Max(value = 31, message = "Dia de fechamento deve ser entre 1 e 31")
    private Integer closingDay;

    @NotNull(message = "Dia de vencimento é obrigatório")
    @Min(value = 1, message = "Dia de vencimento deve ser entre 1 e 31")
    @Max(value = 31, message = "Dia de vencimento deve ser entre 1 e 31")
    private Integer dueDay;

    private Boolean allowsPartialPayment;

    @NotNull(message = "Número da linha é obrigatório")
    private Integer lineNumber;

    public CreditCardImportItemDTO() {
    }

    public CreditCardImportItemDTO(String name, BigDecimal creditLimit, Integer closingDay,
                                   Integer dueDay, Boolean allowsPartialPayment, Integer lineNumber) {
        this.name = name;
        this.creditLimit = creditLimit;
        this.closingDay = closingDay;
        this.dueDay = dueDay;
        this.allowsPartialPayment = allowsPartialPayment;
        this.lineNumber = lineNumber;
    }

    public CreditCard toCreditCard() {
        CreditCard creditCard = new CreditCard();
        creditCard.setName(this.name);
        creditCard.setCreditLimit(this.creditLimit);
        creditCard.setClosingDay(this.closingDay);
        creditCard.setDueDay(this.dueDay);
        creditCard.setAllowsPartialPayment(this.allowsPartialPayment != null ? this.allowsPartialPayment : true);
        return creditCard;
    }

    // Getters and Setters
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

    public Integer getClosingDay() {
        return closingDay;
    }

    public void setClosingDay(Integer closingDay) {
        this.closingDay = closingDay;
    }

    public Integer getDueDay() {
        return dueDay;
    }

    public void setDueDay(Integer dueDay) {
        this.dueDay = dueDay;
    }

    public Boolean getAllowsPartialPayment() {
        return allowsPartialPayment;
    }

    public void setAllowsPartialPayment(Boolean allowsPartialPayment) {
        this.allowsPartialPayment = allowsPartialPayment;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }
}
