package com.truebalance.truebalance.domain.exception;

public class CreditLimitExceededException extends RuntimeException {
    public CreditLimitExceededException(String message) {
        super(message);
    }
}
