package com.truebalance.truebalance.application.dto.input;

import com.truebalance.truebalance.domain.entity.Bill;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BillImportItemDTO {

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    private String name;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String description;

    @NotNull(message = "Data de execução é obrigatória")
    private LocalDateTime executionDate;

    @NotNull(message = "Valor total é obrigatório")
    @Positive(message = "Valor total deve ser positivo")
    private BigDecimal totalAmount;

    @NotNull(message = "Número de parcelas é obrigatório")
    @Min(value = 1, message = "Deve ter pelo menos 1 parcela")
    @Max(value = 120, message = "Máximo de 120 parcelas")
    private Integer numberOfInstallments;

    private Boolean isRecurring;

    private Long creditCardId;

    @NotNull(message = "Número da linha é obrigatório")
    private Integer lineNumber;

    public BillImportItemDTO() {
    }

    public BillImportItemDTO(String name, String description, LocalDateTime executionDate,
                             BigDecimal totalAmount, Integer numberOfInstallments,
                             Boolean isRecurring, Long creditCardId, Integer lineNumber) {
        this.name = name;
        this.description = description;
        this.executionDate = executionDate;
        this.totalAmount = totalAmount;
        this.numberOfInstallments = numberOfInstallments;
        this.isRecurring = isRecurring;
        this.creditCardId = creditCardId;
        this.lineNumber = lineNumber;
    }

    public Bill toBill() {
        Bill bill = new Bill();
        bill.setName(this.name);
        bill.setDescription(this.description);
        bill.setExecutionDate(this.executionDate);
        bill.setTotalAmount(this.totalAmount);
        bill.setNumberOfInstallments(this.numberOfInstallments);
        bill.setIsRecurring(this.isRecurring != null ? this.isRecurring : false);
        return bill;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Boolean getIsRecurring() {
        return isRecurring;
    }

    public void setIsRecurring(Boolean isRecurring) {
        this.isRecurring = isRecurring;
    }

    public Long getCreditCardId() {
        return creditCardId;
    }

    public void setCreditCardId(Long creditCardId) {
        this.creditCardId = creditCardId;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }
}
