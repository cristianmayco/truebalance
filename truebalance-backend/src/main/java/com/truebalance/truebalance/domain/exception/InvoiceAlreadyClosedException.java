package com.truebalance.truebalance.domain.exception;

public class InvoiceAlreadyClosedException extends RuntimeException {
    public InvoiceAlreadyClosedException(Long id) {
        super("A fatura com ID " + id + " já foi fechada anteriormente");
    }
}
