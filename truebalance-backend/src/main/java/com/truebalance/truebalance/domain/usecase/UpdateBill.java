package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.domain.entity.Bill;
import com.truebalance.truebalance.domain.port.BillRepositoryPort;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public class UpdateBill {

    private final BillRepositoryPort repository;

    public UpdateBill(BillRepositoryPort repository) {
        this.repository = repository;
    }

    public Optional<Bill> updateBill(Long id, Bill bill) {
        Optional<Bill> existingBill = repository.findById(id);

        if (existingBill.isEmpty()) {
            return Optional.empty();
        }

        bill.setId(id);

        if (bill.getNumberOfInstallments() > 1) {
            BigDecimal installmentAmount = bill.getTotalAmount().divide(
                BigDecimal.valueOf(bill.getNumberOfInstallments()),
                2,
                RoundingMode.HALF_UP
            );
            bill.setInstallmentAmount(installmentAmount);
        } else {
            bill.setInstallmentAmount(bill.getTotalAmount());
        }

        Bill updatedBill = repository.save(bill);
        return Optional.of(updatedBill);
    }
}
