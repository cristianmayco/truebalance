package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.domain.entity.CreditCard;
import com.truebalance.truebalance.domain.port.CreditCardRepositoryPort;
import com.truebalance.truebalance.domain.service.CreditCardNameNormalizer;

public class CreateCreditCard {

    private final CreditCardRepositoryPort repository;

    public CreateCreditCard(CreditCardRepositoryPort repository) {
        this.repository = repository;
    }

    public CreditCard execute(CreditCard creditCard) {
        // Normalizar nome do cartão para garantir unicidade e consistência
        String normalizedName = CreditCardNameNormalizer.normalize(creditCard.getName());
        creditCard.setName(normalizedName);

        // BR-CC-002: Validate days are between 1-31
        validateDay(creditCard.getClosingDay(), "Closing day");
        validateDay(creditCard.getDueDay(), "Due day");

        // BR-CC-003: Validate day ordering (closingDay < dueDay or handle month wrap)
        // Simple validation: closingDay should be before dueDay in the same month
        // OR closingDay can be > dueDay (wraps to next month)
        // No strict validation needed - business accepts both scenarios

        return repository.save(creditCard);
    }

    private void validateDay(int day, String fieldName) {
        if (day < 1 || day > 31) {
            throw new IllegalArgumentException(
                fieldName + " must be between 1 and 31, got: " + day
            );
        }
    }
}
