package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.domain.entity.PartialPayment;
import com.truebalance.truebalance.domain.port.PartialPaymentRepositoryPort;

import java.util.List;

/**
 * Use case to get all partial payments belonging to a specific invoice.
 * Returns payments ordered by payment date (most recent first).
 */
public class GetPartialPaymentsByInvoice {

    private final PartialPaymentRepositoryPort partialPaymentRepository;

    public GetPartialPaymentsByInvoice(PartialPaymentRepositoryPort partialPaymentRepository) {
        this.partialPaymentRepository = partialPaymentRepository;
    }

    /**
     * Execute the use case.
     *
     * @param invoiceId the ID of the invoice
     * @return list of partial payments ordered by payment date descending
     */
    public List<PartialPayment> execute(Long invoiceId) {
        return partialPaymentRepository.findByInvoiceId(invoiceId);
    }
}
