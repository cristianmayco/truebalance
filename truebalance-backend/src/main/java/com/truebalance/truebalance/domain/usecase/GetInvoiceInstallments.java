package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.domain.entity.Installment;
import com.truebalance.truebalance.domain.port.InstallmentRepositoryPort;

import java.util.List;

/**
 * Use case to get all installments belonging to a specific invoice.
 * Returns installments ordered by due date.
 */
public class GetInvoiceInstallments {

    private final InstallmentRepositoryPort installmentRepository;

    public GetInvoiceInstallments(InstallmentRepositoryPort installmentRepository) {
        this.installmentRepository = installmentRepository;
    }

    /**
     * Execute the use case.
     *
     * @param invoiceId the ID of the invoice
     * @return list of installments ordered by due date
     */
    public List<Installment> execute(Long invoiceId) {
        return installmentRepository.findByInvoiceId(invoiceId);
    }
}
