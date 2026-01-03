package com.truebalance.truebalance.infra.db.adapter;

import com.truebalance.truebalance.domain.entity.PartialPayment;
import com.truebalance.truebalance.domain.port.PartialPaymentRepositoryPort;
import com.truebalance.truebalance.infra.db.entity.PartialPaymentEntity;
import com.truebalance.truebalance.infra.db.repository.PartialPaymentRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementing PartialPaymentRepositoryPort using Spring Data JPA.
 * Converts between domain entities and JPA entities.
 * Replaces the stub implementation used in Phase 3.
 */
@Component
public class PartialPaymentRepositoryAdapter implements PartialPaymentRepositoryPort {

    private final PartialPaymentRepository repository;

    public PartialPaymentRepositoryAdapter(PartialPaymentRepository repository) {
        this.repository = repository;
    }

    @Override
    public PartialPayment save(PartialPayment partialPayment) {
        PartialPaymentEntity entity = toEntity(partialPayment);
        PartialPaymentEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<PartialPayment> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<PartialPayment> findByInvoiceId(Long invoiceId) {
        return repository.findByInvoiceIdOrderByPaymentDateDesc(invoiceId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal sumByInvoiceId(Long invoiceId) {
        return repository.sumByInvoiceId(invoiceId);
    }

    @Override
    public int countByInvoiceId(Long invoiceId) {
        return repository.countByInvoiceId(invoiceId);
    }

    @Override
    public BigDecimal sumAmountByInvoiceIds(List<Long> invoiceIds) {
        if (invoiceIds == null || invoiceIds.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return repository.sumAmountByInvoiceIds(invoiceIds);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    /**
     * Convert domain PartialPayment to JPA PartialPaymentEntity.
     *
     * @param partialPayment domain entity
     * @return JPA entity
     */
    private PartialPaymentEntity toEntity(PartialPayment partialPayment) {
        PartialPaymentEntity entity = new PartialPaymentEntity();
        entity.setId(partialPayment.getId());
        entity.setInvoiceId(partialPayment.getInvoiceId());
        entity.setAmount(partialPayment.getAmount());
        entity.setPaymentDate(partialPayment.getPaymentDate());
        entity.setDescription(partialPayment.getDescription());
        // createdAt is managed by JPA lifecycle hooks
        return entity;
    }

    /**
     * Convert JPA PartialPaymentEntity to domain PartialPayment.
     *
     * @param entity JPA entity
     * @return domain entity
     */
    private PartialPayment toDomain(PartialPaymentEntity entity) {
        PartialPayment partialPayment = new PartialPayment();
        partialPayment.setId(entity.getId());
        partialPayment.setInvoiceId(entity.getInvoiceId());
        partialPayment.setAmount(entity.getAmount());
        partialPayment.setPaymentDate(entity.getPaymentDate());
        partialPayment.setDescription(entity.getDescription());
        partialPayment.setCreatedAt(entity.getCreatedAt());
        return partialPayment;
    }
}
