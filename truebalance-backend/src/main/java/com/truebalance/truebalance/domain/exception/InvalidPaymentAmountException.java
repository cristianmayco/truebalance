package com.truebalance.truebalance.domain.exception;

public class InvalidPaymentAmountException extends RuntimeException {
    public InvalidPaymentAmountException(String message) {
        super(message);
    }
}
