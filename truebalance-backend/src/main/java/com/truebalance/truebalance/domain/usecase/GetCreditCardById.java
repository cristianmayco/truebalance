package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.domain.entity.CreditCard;
import com.truebalance.truebalance.domain.port.CreditCardRepositoryPort;

import java.util.Optional;

public class GetCreditCardById {

    private final CreditCardRepositoryPort repository;

    public GetCreditCardById(CreditCardRepositoryPort repository) {
        this.repository = repository;
    }

    public Optional<CreditCard> execute(Long id) {
        return repository.findById(id);
    }
}
