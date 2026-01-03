package com.truebalance.truebalance.domain.port;

import com.truebalance.truebalance.domain.entity.CreditCard;

import java.util.List;
import java.util.Optional;

public interface CreditCardRepositoryPort {

    CreditCard save(CreditCard creditCard);

    Optional<CreditCard> findById(Long id);

    List<CreditCard> findAll();

    void deleteById(Long id);
}
