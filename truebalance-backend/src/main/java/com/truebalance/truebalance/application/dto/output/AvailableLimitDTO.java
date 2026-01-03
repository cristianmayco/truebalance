package com.truebalance.truebalance.application.dto.output;

import com.truebalance.truebalance.domain.usecase.AvailableLimitResult;

import java.math.BigDecimal;

public class AvailableLimitDTO {

    private Long creditCardId;
    private BigDecimal creditLimit;
    private BigDecimal usedLimit;
    private BigDecimal partialPaymentsTotal;
    private BigDecimal availableLimit;

    public AvailableLimitDTO() {
    }

    public AvailableLimitDTO(Long creditCardId, BigDecimal creditLimit, BigDecimal usedLimit,
                             BigDecimal partialPaymentsTotal, BigDecimal availableLimit) {
        this.creditCardId = creditCardId;
        this.creditLimit = creditLimit;
        this.usedLimit = usedLimit;
        this.partialPaymentsTotal = partialPaymentsTotal;
        this.availableLimit = availableLimit;
    }

    public static AvailableLimitDTO fromResult(AvailableLimitResult result) {
        return new AvailableLimitDTO(
                result.getCreditCardId(),
                result.getCreditLimit(),
                result.getUsedLimit(),
                result.getPartialPaymentsTotal(),
                result.getAvailableLimit()
        );
    }

    public Long getCreditCardId() {
        return creditCardId;
    }

    public void setCreditCardId(Long creditCardId) {
        this.creditCardId = creditCardId;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public BigDecimal getUsedLimit() {
        return usedLimit;
    }

    public void setUsedLimit(BigDecimal usedLimit) {
        this.usedLimit = usedLimit;
    }

    public BigDecimal getPartialPaymentsTotal() {
        return partialPaymentsTotal;
    }

    public void setPartialPaymentsTotal(BigDecimal partialPaymentsTotal) {
        this.partialPaymentsTotal = partialPaymentsTotal;
    }

    public BigDecimal getAvailableLimit() {
        return availableLimit;
    }

    public void setAvailableLimit(BigDecimal availableLimit) {
        this.availableLimit = availableLimit;
    }
}
