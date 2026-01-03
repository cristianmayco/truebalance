package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.domain.entity.CreditCard;
import com.truebalance.truebalance.domain.port.CreditCardRepositoryPort;

import java.util.Optional;

public class DeleteCreditCard {

    private final CreditCardRepositoryPort repository;

    public DeleteCreditCard(CreditCardRepositoryPort repository) {
        this.repository = repository;
    }

    public boolean execute(Long id) {
        Optional<CreditCard> creditCard = repository.findById(id);

        if (creditCard.isEmpty()) {
            return false;
        }

        // BR-CC-009: Prevent delete if has invoices/bills
        // Note: SKIP validation for now - no Invoice entity yet
        // Will add validation in Phase 3 when Invoice is implemented

        repository.deleteById(id);
        return true;
    }
}
