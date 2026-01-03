package com.truebalance.truebalance.domain.exception;

public class BillNotFoundException extends RuntimeException {
    public BillNotFoundException(Long id) {
        super("Conta não encontrada com ID: " + id);
    }
}
