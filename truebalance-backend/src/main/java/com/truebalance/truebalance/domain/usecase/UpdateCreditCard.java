package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.domain.entity.CreditCard;
import com.truebalance.truebalance.domain.port.CreditCardRepositoryPort;

import java.util.Optional;

public class UpdateCreditCard {

    private final CreditCardRepositoryPort repository;

    public UpdateCreditCard(CreditCardRepositoryPort repository) {
        this.repository = repository;
    }

    public Optional<CreditCard> execute(Long id, CreditCard creditCard) {
        Optional<CreditCard> existing = repository.findById(id);

        if (existing.isEmpty()) {
            return Optional.empty();
        }

        // BR-CC-002: Validate days are between 1-31
        validateDay(creditCard.getClosingDay(), "Closing day");
        validateDay(creditCard.getDueDay(), "Due day");

        // BR-CC-003: Validate day ordering (flexible)

        creditCard.setId(id);
        CreditCard updated = repository.save(creditCard);
        return Optional.of(updated);
    }

    private void validateDay(int day, String fieldName) {
        if (day < 1 || day > 31) {
            throw new IllegalArgumentException(
                fieldName + " must be between 1 and 31, got: " + day
            );
        }
    }
}
