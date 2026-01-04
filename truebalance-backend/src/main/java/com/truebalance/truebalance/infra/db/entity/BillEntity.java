package com.truebalance.truebalance.infra.db.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bills")
public class BillEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "execution_date", nullable = false)
    private LocalDateTime executionDate;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "number_of_installments", nullable = false)
    private int numberOfInstallments;

    @Column(name = "installment_amount", precision = 10, scale = 2)
    private BigDecimal installmentAmount;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_recurring", nullable = false)
    private Boolean isRecurring = false;

    @Column(name = "category")
    private String category;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public BillEntity() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public BillEntity(String name, LocalDateTime executionDate, BigDecimal totalAmount, int numberOfInstallments, BigDecimal installmentAmount, String description) {
        this.name = name;
        this.executionDate = executionDate;
        this.totalAmount = totalAmount;
        this.numberOfInstallments = numberOfInstallments;
        this.installmentAmount = installmentAmount;
        this.description = description;
        this.isRecurring = false;
    }

    public BillEntity(String name, LocalDateTime executionDate, BigDecimal totalAmount, int numberOfInstallments, BigDecimal installmentAmount, String description, Boolean isRecurring) {
        this.name = name;
        this.executionDate = executionDate;
        this.totalAmount = totalAmount;
        this.numberOfInstallments = numberOfInstallments;
        this.installmentAmount = installmentAmount;
        this.description = description;
        this.isRecurring = isRecurring != null ? isRecurring : false;
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
        return isRecurring;
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

}
