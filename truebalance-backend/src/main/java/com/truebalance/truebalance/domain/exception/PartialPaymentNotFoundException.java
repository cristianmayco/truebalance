package com.truebalance.truebalance.domain.exception;

public class PartialPaymentNotFoundException extends RuntimeException {
    public PartialPaymentNotFoundException(Long id) {
        super("Pagamento parcial não encontrado com ID: " + id);
    }
}
