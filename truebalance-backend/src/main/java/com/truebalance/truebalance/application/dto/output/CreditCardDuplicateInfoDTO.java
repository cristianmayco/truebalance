package com.truebalance.truebalance.application.dto.output;

import java.math.BigDecimal;

public class CreditCardDuplicateInfoDTO {

    private Integer lineNumber;
    private String name;
    private BigDecimal creditLimit;
    private Integer closingDay;
    private Integer dueDay;
    private Long existingCreditCardId;
    private String reason;

    public CreditCardDuplicateInfoDTO() {
    }

    public CreditCardDuplicateInfoDTO(Integer lineNumber, String name, BigDecimal creditLimit,
                                      Integer closingDay, Integer dueDay, Long existingCreditCardId, String reason) {
        this.lineNumber = lineNumber;
        this.name = name;
        this.creditLimit = creditLimit;
        this.closingDay = closingDay;
        this.dueDay = dueDay;
        this.existingCreditCardId = existingCreditCardId;
        this.reason = reason;
    }

    public Integer getLineNumber() { return lineNumber; }
    public void setLineNumber(Integer lineNumber) { this.lineNumber = lineNumber; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }
    public Integer getClosingDay() { return closingDay; }
    public void setClosingDay(Integer closingDay) { this.closingDay = closingDay; }
    public Integer getDueDay() { return dueDay; }
    public void setDueDay(Integer dueDay) { this.dueDay = dueDay; }
    public Long getExistingCreditCardId() { return existingCreditCardId; }
    public void setExistingCreditCardId(Long existingCreditCardId) { this.existingCreditCardId = existingCreditCardId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
