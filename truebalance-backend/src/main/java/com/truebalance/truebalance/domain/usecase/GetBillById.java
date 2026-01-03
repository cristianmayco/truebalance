package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.domain.entity.Bill;
import com.truebalance.truebalance.domain.port.BillRepositoryPort;

import java.util.Optional;

public class GetBillById {

    private final BillRepositoryPort repository;

    public GetBillById(BillRepositoryPort repository) {
        this.repository = repository;
    }

    public Optional<Bill> execute(Long id) {
        return repository.findById(id);
    }
}
