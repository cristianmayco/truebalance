package com.truebalance.truebalance.domain.exception;

public class CreditCardNotFoundException extends RuntimeException {
    public CreditCardNotFoundException(Long id) {
        super("Cartão de crédito não encontrado com ID: " + id);
    }

    public CreditCardNotFoundException(String message) {
        super(message);
    }
}
