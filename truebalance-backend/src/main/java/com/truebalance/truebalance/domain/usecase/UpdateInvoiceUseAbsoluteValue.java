package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.domain.entity.Invoice;
import com.truebalance.truebalance.domain.port.InvoiceRepositoryPort;

import java.util.Optional;

/**
 * Use case to update the useAbsoluteValue flag of an invoice.
 * 
 * When useAbsoluteValue = true:
 * - The invoice totalAmount is not recalculated from installments
 * - Useful for old invoices where not all bills are registered
 * 
 * When useAbsoluteValue = false (default):
 * - The invoice totalAmount is calculated from the sum of installments
 */
public class UpdateInvoiceUseAbsoluteValue {

    private final InvoiceRepositoryPort invoiceRepository;

    public UpdateInvoiceUseAbsoluteValue(InvoiceRepositoryPort invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    /**
     * Update the useAbsoluteValue flag for an invoice.
     *
     * @param invoiceId the ID of the invoice
     * @param useAbsoluteValue the new value for the flag
     * @return Optional containing the updated invoice, or empty if not found
     */
    public Optional<Invoice> execute(Long invoiceId, boolean useAbsoluteValue) {
        Optional<Invoice> invoiceOpt = invoiceRepository.findById(invoiceId);

        if (invoiceOpt.isEmpty()) {
            return Optional.empty();
        }

        Invoice invoice = invoiceOpt.get();
        invoice.setUseAbsoluteValue(useAbsoluteValue);
        
        Invoice updated = invoiceRepository.save(invoice);
        return Optional.of(updated);
    }
}
