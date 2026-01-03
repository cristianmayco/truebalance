package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.domain.entity.Installment;
import com.truebalance.truebalance.domain.port.InstallmentRepositoryPort;

import java.util.List;

/**
 * Use case to get all installments belonging to a specific bill.
 * Returns installments ordered by installment number (1, 2, 3, ...).
 */
public class GetBillInstallments {

    private final InstallmentRepositoryPort installmentRepository;

    public GetBillInstallments(InstallmentRepositoryPort installmentRepository) {
        this.installmentRepository = installmentRepository;
    }

    /**
     * Execute the use case.
     *
     * @param billId the ID of the bill
     * @return list of installments ordered by installment number
     */
    public List<Installment> execute(Long billId) {
        return installmentRepository.findByBillId(billId);
    }
}
