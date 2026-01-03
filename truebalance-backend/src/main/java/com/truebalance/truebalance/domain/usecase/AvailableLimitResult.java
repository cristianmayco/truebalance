package com.truebalance.truebalance.domain.usecase;

import java.math.BigDecimal;

/**
 * Result object for GetAvailableLimit use case.
 * Contains detailed information about credit card limit calculation.
 */
public class AvailableLimitResult {
    private final Long creditCardId;
    private final BigDecimal creditLimit;
    private final BigDecimal usedLimit;
    private final BigDecimal partialPaymentsTotal;
    private final BigDecimal availableLimit;

    public AvailableLimitResult(Long creditCardId, BigDecimal creditLimit, BigDecimal usedLimit,
                                BigDecimal partialPaymentsTotal, BigDecimal availableLimit) {
        this.creditCardId = creditCardId;
        this.creditLimit = creditLimit;
        this.usedLimit = usedLimit;
        this.partialPaymentsTotal = partialPaymentsTotal;
        this.availableLimit = availableLimit;
    }

    public Long getCreditCardId() {
        return creditCardId;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public BigDecimal getUsedLimit() {
        return usedLimit;
    }

    public BigDecimal getPartialPaymentsTotal() {
        return partialPaymentsTotal;
    }

    public BigDecimal getAvailableLimit() {
        return availableLimit;
    }
}
