package com.truebalance.truebalance.infra.db.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_cards")
public class CreditCardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "credit_limit", nullable = false, precision = 10, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "closing_day", nullable = false)
    private int closingDay;

    @Column(name = "due_day", nullable = false)
    private int dueDay;

    @Column(name = "allows_partial_payment", nullable = false)
    private boolean allowsPartialPayment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public CreditCardEntity() {
    }

    public CreditCardEntity(String name, BigDecimal creditLimit, int closingDay, int dueDay, boolean allowsPartialPayment) {
        this.name = name;
        this.creditLimit = creditLimit;
        this.closingDay = closingDay;
        this.dueDay = dueDay;
        this.allowsPartialPayment = allowsPartialPayment;
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
