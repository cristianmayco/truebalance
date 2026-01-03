package com.truebalance.truebalance.infra.db.adapter;

import com.truebalance.truebalance.domain.entity.Installment;
import com.truebalance.truebalance.domain.port.InstallmentRepositoryPort;
import com.truebalance.truebalance.infra.db.entity.InstallmentEntity;
import com.truebalance.truebalance.infra.db.repository.InstallmentRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapter implementing InstallmentRepositoryPort using Spring Data JPA.
 * Converts between domain entities and JPA entities.
 */
@Component
public class InstallmentRepositoryAdapter implements InstallmentRepositoryPort {

    private final InstallmentRepository repository;

    public InstallmentRepositoryAdapter(InstallmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Installment save(Installment installment) {
        InstallmentEntity entity = toEntity(installment);
        InstallmentEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<Installment> saveAll(List<Installment> installments) {
        List<InstallmentEntity> entities = installments.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
        List<InstallmentEntity> saved = repository.saveAll(entities);
        return saved.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Installment> findByBillId(Long billId) {
        return repository.findByBillIdOrderByInstallmentNumberAsc(billId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Installment> findByInvoiceId(Long invoiceId) {
        return repository.findByInvoiceIdOrderByDueDateAsc(invoiceId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByBillId(Long billId) {
        repository.deleteByBillId(billId);
    }

    @Override
    public BigDecimal sumAmountByInvoiceIds(List<Long> invoiceIds) {
        if (invoiceIds == null || invoiceIds.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return repository.sumAmountByInvoiceIds(invoiceIds);
    }

    /**
     * Convert domain Installment to JPA InstallmentEntity.
     *
     * @param installment domain entity
     * @return JPA entity
     */
    private InstallmentEntity toEntity(Installment installment) {
        InstallmentEntity entity = new InstallmentEntity();
        entity.setId(installment.getId());
        entity.setBillId(installment.getBillId());
        entity.setCreditCardId(installment.getCreditCardId());
        entity.setInvoiceId(installment.getInvoiceId());
        entity.setInstallmentNumber(installment.getInstallmentNumber());
        entity.setAmount(installment.getAmount());
        entity.setDueDate(installment.getDueDate());
        // createdAt is managed by JPA lifecycle hooks
        return entity;
    }

    /**
     * Convert JPA InstallmentEntity to domain Installment.
     *
     * @param entity JPA entity
     * @return domain entity
     */
    private Installment toDomain(InstallmentEntity entity) {
        Installment installment = new Installment();
        installment.setId(entity.getId());
        installment.setBillId(entity.getBillId());
        installment.setCreditCardId(entity.getCreditCardId());
        installment.setInvoiceId(entity.getInvoiceId());
        installment.setInstallmentNumber(entity.getInstallmentNumber());
        installment.setAmount(entity.getAmount());
        installment.setDueDate(entity.getDueDate());
        installment.setCreatedAt(entity.getCreatedAt());
        return installment;
    }
}
