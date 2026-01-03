package com.truebalance.truebalance.application.dto.output;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DuplicateInfoDTO {

    private Integer lineNumber;
    private String name;
    private BigDecimal totalAmount;
    private LocalDateTime executionDate;
    private Integer numberOfInstallments;
    private Long existingBillId;
    private String reason;

    public DuplicateInfoDTO() {
    }

    public DuplicateInfoDTO(Integer lineNumber, String name, BigDecimal totalAmount,
                            LocalDateTime executionDate, Integer numberOfInstallments,
                            Long existingBillId, String reason) {
        this.lineNumber = lineNumber;
        this.name = name;
        this.totalAmount = totalAmount;
        this.executionDate = executionDate;
        this.numberOfInstallments = numberOfInstallments;
        this.existingBillId = existingBillId;
        this.reason = reason;
    }

    // Getters and Setters
    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(LocalDateTime executionDate) {
        this.executionDate = executionDate;
    }

    public Integer getNumberOfInstallments() {
        return numberOfInstallments;
    }

    public void setNumberOfInstallments(Integer numberOfInstallments) {
        this.numberOfInstallments = numberOfInstallments;
    }

    public Long getExistingBillId() {
        return existingBillId;
    }

    public void setExistingBillId(Long existingBillId) {
        this.existingBillId = existingBillId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
