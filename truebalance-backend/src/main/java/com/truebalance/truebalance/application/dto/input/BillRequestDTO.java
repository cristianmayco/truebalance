package com.truebalance.truebalance.application.dto.input;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.truebalance.truebalance.domain.entity.Bill;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BillRequestDTO {

    @NotBlank(message = "Nome da conta é obrigatório")
    private String name;

    @NotNull(message = "Data de execução é obrigatória")
    private LocalDateTime executionDate;

    @NotNull(message = "Valor total é obrigatório")
    @Positive(message = "Valor total deve ser positivo")
    private BigDecimal totalAmount;

    @NotNull(message = "Número de parcelas é obrigatório")
    @Min(value = 1, message = "Número de parcelas deve ser no mínimo 1")
    private Integer numberOfInstallments;

    private String description;  // Optional

    private Boolean isRecurring;  // Optional: if true, bill is recurring (e.g., monthly internet bill)

    private String category;  // Optional: category name (deprecated, use categoryId instead)
    private Long categoryId;  // Optional: ID of the category

    private Long creditCardId;  // Optional: if provided, bill will be linked to credit card

    // Default constructor required for Jackson deserialization
    public BillRequestDTO() {
    }

    public BillRequestDTO(String name, LocalDateTime executionDate, BigDecimal totalAmount, Integer numberOfInstallments, String description, Long creditCardId) {
        this.name = name;
        this.executionDate = executionDate;
        this.totalAmount = totalAmount;
        this.numberOfInstallments = numberOfInstallments;
        this.description = description;
        this.isRecurring = false;
        this.creditCardId = creditCardId;
    }

    public BillRequestDTO(String name, LocalDateTime executionDate, BigDecimal totalAmount, Integer numberOfInstallments, String description, Boolean isRecurring, Long creditCardId) {
        this.name = name;
        this.executionDate = executionDate;
        this.totalAmount = totalAmount;
        this.numberOfInstallments = numberOfInstallments;
        this.description = description;
        this.isRecurring = isRecurring != null ? isRecurring : false;
        this.creditCardId = creditCardId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(LocalDateTime executionDate) {
        this.executionDate = executionDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getNumberOfInstallments() {
        return numberOfInstallments;
    }

    public void setNumberOfInstallments(Integer numberOfInstallments) {
        this.numberOfInstallments = numberOfInstallments;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCreditCardId() {
        return creditCardId;
    }

    public void setCreditCardId(Long creditCardId) {
        this.creditCardId = creditCardId;
    }

    public Boolean getIsRecurring() {
        return isRecurring != null ? isRecurring : false;
    }

    public void setIsRecurring(Boolean isRecurring) {
        this.isRecurring = isRecurring != null ? isRecurring : false;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Bill toBill(){
        Bill bill = new Bill();
        bill.setName(this.getName());
        bill.setExecutionDate(this.getExecutionDate());
        bill.setTotalAmount(this.getTotalAmount());
        bill.setNumberOfInstallments(this.getNumberOfInstallments());
        bill.setDescription(this.getDescription());
        bill.setIsRecurring(this.getIsRecurring());
        // category será preenchido pelo controller se categoryId for fornecido
        bill.setCategory(this.getCategory());

        return bill;
    }
}
