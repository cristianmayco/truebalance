package com.truebalance.truebalance.application.dto.input;

import com.truebalance.truebalance.domain.entity.CreditCard;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class CreditCardRequestDTO {

    @NotBlank(message = "Nome do cartão é obrigatório")
    private String name;

    @NotNull(message = "Limite de crédito é obrigatório")
    @Positive(message = "Limite de crédito deve ser positivo")
    private BigDecimal creditLimit;

    @NotNull(message = "Dia de fechamento é obrigatório")
    @Min(value = 1, message = "Dia de fechamento deve estar entre 1 e 31")
    @Max(value = 31, message = "Dia de fechamento deve estar entre 1 e 31")
    private Integer closingDay;

    @NotNull(message = "Dia de vencimento é obrigatório")
    @Min(value = 1, message = "Dia de vencimento deve estar entre 1 e 31")
    @Max(value = 31, message = "Dia de vencimento deve estar entre 1 e 31")
    private Integer dueDay;

    @NotNull(message = "Campo 'permite pagamento parcial' é obrigatório")
    private Boolean allowsPartialPayment;

    public CreditCardRequestDTO() {
    }

    public CreditCardRequestDTO(String name, BigDecimal creditLimit, Integer closingDay, Integer dueDay, Boolean allowsPartialPayment) {
        this.name = name;
        this.creditLimit = creditLimit;
        this.closingDay = closingDay;
        this.dueDay = dueDay;
        this.allowsPartialPayment = allowsPartialPayment;
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

    public Boolean isAllowsPartialPayment() {
        return allowsPartialPayment;
    }

    public void setAllowsPartialPayment(Boolean allowsPartialPayment) {
        this.allowsPartialPayment = allowsPartialPayment;
    }

    public CreditCard toCreditCard() {
        CreditCard creditCard = new CreditCard();
        creditCard.setName(this.name);
        creditCard.setCreditLimit(this.creditLimit);
        creditCard.setClosingDay(this.closingDay);
        creditCard.setDueDay(this.dueDay);
        creditCard.setAllowsPartialPayment(this.allowsPartialPayment);
        return creditCard;
    }
}
