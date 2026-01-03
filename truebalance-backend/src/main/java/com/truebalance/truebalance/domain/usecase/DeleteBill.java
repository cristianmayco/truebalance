package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.domain.entity.Bill;
import com.truebalance.truebalance.domain.port.BillRepositoryPort;

import java.util.Optional;

public class DeleteBill {

    private final BillRepositoryPort repository;

    public DeleteBill(BillRepositoryPort repository) {
        this.repository = repository;
    }

    public boolean execute(Long id) {
        Optional<Bill> bill = repository.findById(id);

        if (bill.isEmpty()) {
            return false;
        }

        // BR-B-003: Delete em cascata (installments)
        // Note: Cascade delete will be handled automatically by JPA when Installment entity is created
        // BR-I-007: Prevent delete if in closed invoice - SKIP for now (no invoices yet)

        repository.deleteById(id);
        return true;
    }
}
