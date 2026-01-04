package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.domain.entity.Bill;
import com.truebalance.truebalance.domain.port.BillRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class GetAllBills {

    private static final Logger logger = LoggerFactory.getLogger(GetAllBills.class);
    private final BillRepositoryPort repository;

    public GetAllBills(BillRepositoryPort repository) {
        this.repository = repository;
    }

    public List<Bill> execute() {
        logger.info("Buscando todas as contas");
        List<Bill> bills = repository.findAll();
        logger.info("Total de contas encontradas: {}", bills.size());
        return bills;
    }

    public Page<Bill> execute(Pageable pageable) {
        logger.info("Buscando contas com paginação: page={}, size={}, sort={}", 
            pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        Page<Bill> bills = repository.findAll(pageable);
        logger.info("Contas encontradas: {} de {} total", bills.getNumberOfElements(), bills.getTotalElements());
        return bills;
    }

    public Page<Bill> execute(Pageable pageable, String name, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        logger.info("Buscando contas com filtros: page={}, size={}, name={}, startDate={}, endDate={}", 
            pageable.getPageNumber(), pageable.getPageSize(), name, startDate, endDate);
        Page<Bill> bills = repository.findAll(pageable, name, startDate, endDate);
        logger.info("Contas encontradas com filtros: {} de {} total", bills.getNumberOfElements(), bills.getTotalElements());
        return bills;
    }

    public Page<Bill> execute(Pageable pageable, String name, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate,
                              java.math.BigDecimal minAmount, java.math.BigDecimal maxAmount,
                              Integer numberOfInstallments, String category, Long creditCardId, Boolean hasCreditCard) {
        logger.info("Buscando contas com filtros avançados: page={}, size={}, name={}, startDate={}, endDate={}, " +
                   "minAmount={}, maxAmount={}, numberOfInstallments={}, category={}, creditCardId={}, hasCreditCard={}", 
            pageable.getPageNumber(), pageable.getPageSize(), name, startDate, endDate,
            minAmount, maxAmount, numberOfInstallments, category, creditCardId, hasCreditCard);
        Page<Bill> bills = repository.findAll(pageable, name, startDate, endDate, minAmount, maxAmount,
                                              numberOfInstallments, category, creditCardId, hasCreditCard);
        logger.info("Contas encontradas com filtros avançados: {} de {} total", bills.getNumberOfElements(), bills.getTotalElements());
        return bills;
    }
}
