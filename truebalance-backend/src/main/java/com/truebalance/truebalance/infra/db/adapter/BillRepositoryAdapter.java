package com.truebalance.truebalance.infra.db.adapter;

import com.truebalance.truebalance.domain.entity.Bill;
import com.truebalance.truebalance.domain.port.BillRepositoryPort;
import com.truebalance.truebalance.infra.db.entity.BillEntity;
import com.truebalance.truebalance.infra.db.repository.BillRepository;
import com.truebalance.truebalance.infra.db.specification.BillSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class BillRepositoryAdapter implements BillRepositoryPort {

    private static final Logger logger = LoggerFactory.getLogger(BillRepositoryAdapter.class);
    private final BillRepository repository;

    public BillRepositoryAdapter(BillRepository repository) {
        this.repository = repository;
    }

    @Override
    public Bill save(Bill bill) {
        logger.debug("Salvando conta no banco de dados: nome={}", bill.getName());
        BillEntity entity = toEntity(bill);
        BillEntity savedEntity = repository.save(entity);
        logger.debug("Conta salva com sucesso! ID={}", savedEntity.getId());
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Bill> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Bill> findAll() {
        logger.debug("Buscando todas as contas sem paginação");
        return repository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Page<Bill> findAll(Pageable pageable) {
        logger.debug("Buscando contas com paginação: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<BillEntity> entities = repository.findAll(pageable);
        return entities.map(this::toDomain);
    }

    @Override
    public Page<Bill> findAll(Pageable pageable, String name, LocalDateTime startDate, LocalDateTime endDate) {
        logger.debug("Buscando contas com filtros: page={}, size={}, name={}, startDate={}, endDate={}",
            pageable.getPageNumber(), pageable.getPageSize(), name, startDate, endDate);

        // Fetch all with name filter and manually filter by dates
        Page<BillEntity> allEntities = repository.findAll(pageable, name, startDate, endDate);

        // Apply date filters manually if needed
        if (startDate != null || endDate != null) {
            List<BillEntity> filteredList = allEntities.getContent().stream()
                .filter(entity -> {
                    if (startDate != null && entity.getExecutionDate().isBefore(startDate)) {
                        return false;
                    }
                    if (endDate != null && entity.getExecutionDate().isAfter(endDate)) {
                        return false;
                    }
                    return true;
                })
                .collect(java.util.stream.Collectors.toList());

            logger.debug("Encontradas {} entidades após filtro de data", filteredList.size());
            return new org.springframework.data.domain.PageImpl<>(
                filteredList.stream().map(this::toDomain).collect(java.util.stream.Collectors.toList()),
                pageable,
                filteredList.size()
            );
        }

        logger.debug("Encontradas {} entidades", allEntities.getNumberOfElements());
        return allEntities.map(this::toDomain);
    }

    @Override
    public Page<Bill> findAll(Pageable pageable, String name, LocalDateTime startDate, LocalDateTime endDate,
                              java.math.BigDecimal minAmount, java.math.BigDecimal maxAmount,
                              Integer numberOfInstallments, String category, Long creditCardId, Boolean hasCreditCard) {
        logger.debug("Buscando contas com filtros avançados: page={}, size={}, name={}, startDate={}, endDate={}, " +
                    "minAmount={}, maxAmount={}, numberOfInstallments={}, category={}, creditCardId={}, hasCreditCard={}",
            pageable.getPageNumber(), pageable.getPageSize(), name, startDate, endDate,
            minAmount, maxAmount, numberOfInstallments, category, creditCardId, hasCreditCard);

        Specification<BillEntity> spec = Specification.where(BillSpecification.hasName(name))
                .and(BillSpecification.hasStartDate(startDate))
                .and(BillSpecification.hasEndDate(endDate))
                .and(BillSpecification.hasMinAmount(minAmount))
                .and(BillSpecification.hasMaxAmount(maxAmount))
                .and(BillSpecification.hasNumberOfInstallments(numberOfInstallments))
                .and(BillSpecification.hasCategory(category))
                .and(BillSpecification.hasCreditCard(creditCardId))
                .and(BillSpecification.hasCreditCardFilter(hasCreditCard));

        Page<BillEntity> entities = repository.findAll(spec, pageable);

        logger.debug("Encontradas {} entidades com filtros avançados", entities.getNumberOfElements());
        return entities.map(this::toDomain);
    }

    @Override
    public List<Bill> findAllByCategoryAndDateRange(String categoryName, LocalDateTime startDate, LocalDateTime endDate) {
        logger.debug("Buscando contas por categoria e intervalo de datas: category={}, startDate={}, endDate={}",
                categoryName, startDate, endDate);

        Specification<BillEntity> spec = Specification.where(BillSpecification.hasCategory(categoryName))
                .and(BillSpecification.hasStartDate(startDate))
                .and(BillSpecification.hasEndDate(endDate));

        List<BillEntity> entities = repository.findAll(spec);
        logger.debug("Encontradas {} contas para a categoria no período", entities.size());
        return entities.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    private BillEntity toEntity(Bill bill) {
        BillEntity entity = new BillEntity();
        entity.setId(bill.getId());
        entity.setName(bill.getName());
        entity.setExecutionDate(bill.getExecutionDate());
        entity.setTotalAmount(bill.getTotalAmount());
        entity.setNumberOfInstallments(bill.getNumberOfInstallments());
        entity.setInstallmentAmount(bill.getInstallmentAmount());
        entity.setDescription(bill.getDescription());
        entity.setIsRecurring(bill.getIsRecurring());
        entity.setCategory(bill.getCategory());
        // createdAt e updatedAt são gerenciados automaticamente pelo JPA (@PrePersist e @PreUpdate)
        return entity;
    }

    private Bill toDomain(BillEntity entity) {
        Bill bill = new Bill();
        bill.setId(entity.getId());
        bill.setName(entity.getName());
        bill.setExecutionDate(entity.getExecutionDate());
        bill.setTotalAmount(entity.getTotalAmount());
        bill.setNumberOfInstallments(entity.getNumberOfInstallments());
        bill.setInstallmentAmount(entity.getInstallmentAmount());
        bill.setDescription(entity.getDescription());
        bill.setIsRecurring(entity.getIsRecurring());
        bill.setCategory(entity.getCategory());
        bill.setCreatedAt(entity.getCreatedAt());
        bill.setUpdatedAt(entity.getUpdatedAt());
        return bill;
    }
}
