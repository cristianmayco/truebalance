package com.truebalance.truebalance.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

public class Bill {

    private Long id;
    private String name;
    private LocalDateTime executionDate;
    private BigDecimal totalAmount;
    private int numberOfInstallments;
    private BigDecimal installmentAmount;
    private String description;
    private Boolean isRecurring;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}
