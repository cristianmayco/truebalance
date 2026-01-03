package com.truebalance.truebalance.infra.db.adapter;

import com.truebalance.truebalance.domain.entity.Invoice;
import com.truebalance.truebalance.domain.port.InvoiceRepositoryPort;
import com.truebalance.truebalance.infra.db.entity.InvoiceEntity;
import com.truebalance.truebalance.infra.db.repository.InvoiceRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class InvoiceRepositoryAdapter implements InvoiceRepositoryPort {

    private final InvoiceRepository repository;

    public InvoiceRepositoryAdapter(InvoiceRepository repository) {
        this.repository = repository;
    }

    @Override
    public Invoice save(Invoice invoice) {
        InvoiceEntity entity;

        if (invoice.getId() == null) {
            // NEW entity: Create fresh entity, Hibernate will initialize version to 0
            entity = toEntity(invoice);
        } else {
            // EXISTING entity: Try to fetch from DB to preserve version
            Optional<InvoiceEntity> existing = repository.findById(invoice.getId());

            if (existing.isPresent()) {
                // Entity exists in DB: update its fields to preserve version
                entity = existing.get();
                entity.setCreditCardId(invoice.getCreditCardId());
                entity.setReferenceMonth(invoice.getReferenceMonth());
                entity.setTotalAmount(invoice.getTotalAmount());
                entity.setPreviousBalance(invoice.getPreviousBalance());
                entity.setClosed(invoice.isClosed());
                entity.setPaid(invoice.isPaid());
            } else {
                // Entity has ID but doesn't exist in DB (detached/test scenario)
                // Use toEntity() and let JPA handle it
                entity = toEntity(invoice);
            }
        }

        InvoiceEntity savedEntity = repository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public List<Invoice> saveAll(List<Invoice> invoices) {
        List<InvoiceEntity> entities = invoices.stream()
                .map(invoice -> {
                    if (invoice.getId() == null) {
                        // NEW entity
                        return toEntity(invoice);
                    } else {
                        // EXISTING entity: try to fetch and update
                        Optional<InvoiceEntity> existing = repository.findById(invoice.getId());

                        if (existing.isPresent()) {
                            // Entity exists in DB: update its fields to preserve version
                            InvoiceEntity entity = existing.get();
                            entity.setCreditCardId(invoice.getCreditCardId());
                            entity.setReferenceMonth(invoice.getReferenceMonth());
                            entity.setTotalAmount(invoice.getTotalAmount());
                            entity.setPreviousBalance(invoice.getPreviousBalance());
                            entity.setClosed(invoice.isClosed());
                            entity.setPaid(invoice.isPaid());
                            return entity;
                        } else {
                            // Entity has ID but doesn't exist in DB (detached/test scenario)
                            return toEntity(invoice);
                        }
                    }
                })
                .collect(Collectors.toList());

        List<InvoiceEntity> saved = repository.saveAll(entities);
        return saved.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Invoice> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Invoice> findByCreditCardIdAndReferenceMonth(Long creditCardId, LocalDate referenceMonth) {
        return repository.findByCreditCardIdAndReferenceMonth(creditCardId, referenceMonth)
                .map(this::toDomain);
    }

    @Override
    public List<Invoice> findByCreditCardId(Long creditCardId) {
        return repository.findByCreditCardIdOrderByReferenceMonthDesc(creditCardId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Invoice> findByCreditCardIdAndClosed(Long creditCardId, boolean closed) {
        return repository.findByCreditCardIdAndClosed(creditCardId, closed).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private InvoiceEntity toEntity(Invoice invoice) {
        InvoiceEntity entity = new InvoiceEntity();
        entity.setId(invoice.getId());
        entity.setCreditCardId(invoice.getCreditCardId());
        entity.setReferenceMonth(invoice.getReferenceMonth());
        entity.setTotalAmount(invoice.getTotalAmount());
        entity.setPreviousBalance(invoice.getPreviousBalance());
        entity.setClosed(invoice.isClosed());
        entity.setPaid(invoice.isPaid());
        // createdAt and updatedAt managed by JPA lifecycle hooks
        return entity;
    }

    private Invoice toDomain(InvoiceEntity entity) {
        Invoice invoice = new Invoice();
        invoice.setId(entity.getId());
        invoice.setCreditCardId(entity.getCreditCardId());
        invoice.setReferenceMonth(entity.getReferenceMonth());
        invoice.setTotalAmount(entity.getTotalAmount());
        invoice.setPreviousBalance(entity.getPreviousBalance());
        invoice.setClosed(entity.isClosed());
        invoice.setPaid(entity.isPaid());
        invoice.setCreatedAt(entity.getCreatedAt());
        invoice.setUpdatedAt(entity.getUpdatedAt());
        return invoice;
    }
}
