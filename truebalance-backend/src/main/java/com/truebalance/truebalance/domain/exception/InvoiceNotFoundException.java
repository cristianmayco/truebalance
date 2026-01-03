package com.truebalance.truebalance.domain.exception;

public class InvoiceNotFoundException extends RuntimeException {
    public InvoiceNotFoundException(Long id) {
        super("Fatura não encontrada com ID: " + id);
    }
}
