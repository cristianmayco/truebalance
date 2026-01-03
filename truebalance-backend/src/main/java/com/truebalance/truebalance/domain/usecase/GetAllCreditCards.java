package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.domain.entity.CreditCard;
import com.truebalance.truebalance.domain.port.CreditCardRepositoryPort;

import java.util.List;

public class GetAllCreditCards {

    private final CreditCardRepositoryPort repository;

    public GetAllCreditCards(CreditCardRepositoryPort repository) {
        this.repository = repository;
    }

    public List<CreditCard> execute() {
        return repository.findAll();
    }
}
