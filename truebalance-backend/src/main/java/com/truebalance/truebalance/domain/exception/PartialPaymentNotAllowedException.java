package com.truebalance.truebalance.domain.exception;

public class PartialPaymentNotAllowedException extends RuntimeException {
    public PartialPaymentNotAllowedException(Long creditCardId) {
        super("O cartão de crédito com ID " + creditCardId + " não permite pagamentos parciais");
    }
}
