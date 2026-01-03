package com.truebalance.truebalance.domain.exception;

public class InvoiceClosedException extends RuntimeException {
    public InvoiceClosedException(Long id) {
        super("A fatura com ID " + id + " já está fechada e não pode ser modificada");
    }
}
