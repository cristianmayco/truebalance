package com.truebalance.truebalance.domain.usecase;


import com.truebalance.truebalance.domain.entity.Bill;
import com.truebalance.truebalance.domain.port.BillRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CreateBill {

    private static final Logger logger = LoggerFactory.getLogger(CreateBill.class);
    private BillRepositoryPort repository;


    public CreateBill(BillRepositoryPort repository) {
        this.repository = repository;
    }

    public Bill addBill(Bill bill) {
        logger.info("Criando nova conta: nome={}, valorTotal={}, parcelas={}, dataExecucao={}", 
            bill.getName(), bill.getTotalAmount(), bill.getNumberOfInstallments(), bill.getExecutionDate());
        
        if (bill.getNumberOfInstallments() > 1) {
            BigDecimal installmentAmount = bill.getTotalAmount().divide(
                BigDecimal.valueOf(bill.getNumberOfInstallments()),
                2,
                RoundingMode.HALF_UP
            );
            bill.setInstallmentAmount(installmentAmount);
            logger.debug("Valor da parcela calculado: {}", installmentAmount);
        } else {
            bill.setInstallmentAmount(bill.getTotalAmount());
        }
        
        Bill savedBill = repository.save(bill);
        logger.info("Conta criada com sucesso! ID={}, nome={}", savedBill.getId(), savedBill.getName());
        
        return savedBill;
    }

}
