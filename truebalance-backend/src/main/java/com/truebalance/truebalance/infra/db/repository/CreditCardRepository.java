package com.truebalance.truebalance.infra.db.repository;

import com.truebalance.truebalance.infra.db.entity.CreditCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCardEntity, Long> {
    java.util.Optional<CreditCardEntity> findByName(String name);
}
