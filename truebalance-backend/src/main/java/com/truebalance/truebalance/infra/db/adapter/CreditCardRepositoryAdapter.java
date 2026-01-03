package com.truebalance.truebalance.infra.db.adapter;

import com.truebalance.truebalance.domain.entity.CreditCard;
import com.truebalance.truebalance.domain.port.CreditCardRepositoryPort;
import com.truebalance.truebalance.infra.db.entity.CreditCardEntity;
import com.truebalance.truebalance.infra.db.repository.CreditCardRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CreditCardRepositoryAdapter implements CreditCardRepositoryPort {

    private final CreditCardRepository repository;

    public CreditCardRepositoryAdapter(CreditCardRepository repository) {
        this.repository = repository;
    }

    @Override
    public CreditCard save(CreditCard creditCard) {
        CreditCardEntity entity = toEntity(creditCard);
        CreditCardEntity savedEntity = repository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<CreditCard> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<CreditCard> findAll() {
        return repository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    private CreditCardEntity toEntity(CreditCard creditCard) {
        CreditCardEntity entity = new CreditCardEntity();
        entity.setId(creditCard.getId());
        entity.setName(creditCard.getName());
        entity.setCreditLimit(creditCard.getCreditLimit());
        entity.setClosingDay(creditCard.getClosingDay());
        entity.setDueDay(creditCard.getDueDay());
        entity.setAllowsPartialPayment(creditCard.isAllowsPartialPayment());
        // createdAt e updatedAt são gerenciados automaticamente pelo JPA (@PrePersist e @PreUpdate)
        return entity;
    }

    private CreditCard toDomain(CreditCardEntity entity) {
        CreditCard creditCard = new CreditCard();
        creditCard.setId(entity.getId());
        creditCard.setName(entity.getName());
        creditCard.setCreditLimit(entity.getCreditLimit());
        creditCard.setClosingDay(entity.getClosingDay());
        creditCard.setDueDay(entity.getDueDay());
        creditCard.setAllowsPartialPayment(entity.isAllowsPartialPayment());
        creditCard.setCreatedAt(entity.getCreatedAt());
        creditCard.setUpdatedAt(entity.getUpdatedAt());
        return creditCard;
    }
}
