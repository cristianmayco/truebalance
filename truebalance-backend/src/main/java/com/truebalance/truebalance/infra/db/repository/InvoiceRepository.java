package com.truebalance.truebalance.infra.db.repository;

import com.truebalance.truebalance.infra.db.entity.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<InvoiceEntity, Long> {

    Optional<InvoiceEntity> findByCreditCardIdAndReferenceMonth(Long creditCardId, LocalDate referenceMonth);

    List<InvoiceEntity> findByCreditCardIdOrderByReferenceMonthDesc(Long creditCardId);

    List<InvoiceEntity> findByCreditCardIdAndClosed(Long creditCardId, boolean closed);
}
