package com.truebalance.truebalance.application.dto.output;

import com.truebalance.truebalance.domain.entity.Bill;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BillResponseDTO {

    private Long id;
    private String name;
    private LocalDateTime executionDate;
    private BigDecimal totalAmount;
    private int numberOfInstallments;
    private BigDecimal installmentAmount;
    private String description;
    private Boolean isRecurring;
    private Long creditCardId;  // Credit card ID if bill is linked to a credit card
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BillResponseDTO() {
    }

    public BillResponseDTO(Long id, String name, LocalDateTime executionDate, BigDecimal totalAmount,
                           int numberOfInstallments, BigDecimal installmentAmount, String description,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.executionDate = executionDate;
        this.totalAmount = totalAmount;
        this.numberOfInstallments = numberOfInstallments;
        this.installmentAmount = installmentAmount;
        this.description = description;
        this.isRecurring = false;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public BillResponseDTO(Long id, String name, LocalDateTime executionDate, BigDecimal totalAmount,
                           int numberOfInstallments, BigDecimal installmentAmount, String description,
                           Boolean isRecurring, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.executionDate = executionDate;
        this.totalAmount = totalAmount;
        this.numberOfInstallments = numberOfInstallments;
        this.installmentAmount = installmentAmount;
        this.description = description;
        this.isRecurring = isRecurring != null ? isRecurring : false;
        this.creditCardId = null;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public BillResponseDTO(Long id, String name, LocalDateTime executionDate, BigDecimal totalAmount,
                           int numberOfInstallments, BigDecimal installmentAmount, String description,
                           Boolean isRecurring, Long creditCardId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.executionDate = executionDate;
        this.totalAmount = totalAmount;
        this.numberOfInstallments = numberOfInstallments;
        this.installmentAmount = installmentAmount;
        this.description = description;
        this.isRecurring = isRecurring != null ? isRecurring : false;
        this.creditCardId = creditCardId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static BillResponseDTO fromBill(Bill bill) {
        return new BillResponseDTO(
                bill.getId(),
                bill.getName(),
                bill.getExecutionDate(),
                bill.getTotalAmount(),
                bill.getNumberOfInstallments(),
                bill.getInstallmentAmount(),
                bill.getDescription(),
                bill.getIsRecurring(),
                null,  // creditCardId will be set separately if needed
                bill.getCreatedAt(),
                bill.getUpdatedAt()
        );
    }

    public static BillResponseDTO fromBill(Bill bill, Long creditCardId) {
        return new BillResponseDTO(
                bill.getId(),
                bill.getName(),
                bill.getExecutionDate(),
                bill.getTotalAmount(),
                bill.getNumberOfInstallments(),
                bill.getInstallmentAmount(),
                bill.getDescription(),
                bill.getIsRecurring(),
                creditCardId,
                bill.getCreatedAt(),
                bill.getUpdatedAt()
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

    public int getNumberOfInstallments() {
        return numberOfInstallments;
    }

    public void setNumberOfInstallments(int numberOfInstallments) {
        this.numberOfInstallments = numberOfInstallments;
    }

    public BigDecimal getInstallmentAmount() {
        return installmentAmount;
    }

    public void setInstallmentAmount(BigDecimal installmentAmount) {
        this.installmentAmount = installmentAmount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Boolean getIsRecurring() {
        return isRecurring != null ? isRecurring : false;
    }

    public void setIsRecurring(Boolean isRecurring) {
        this.isRecurring = isRecurring != null ? isRecurring : false;
    }

    public Long getCreditCardId() {
        return creditCardId;
    }

    public void setCreditCardId(Long creditCardId) {
        this.creditCardId = creditCardId;
    }
}
