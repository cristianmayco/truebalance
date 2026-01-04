package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.domain.entity.Invoice;
import com.truebalance.truebalance.domain.port.InvoiceRepositoryPort;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Use case to update the total amount of an invoice.
 *
 * This operation is only allowed when useAbsoluteValue = true.
 * When useAbsoluteValue is false, the total is automatically calculated from installments.
 */
public class UpdateInvoiceTotalAmount {

    private final InvoiceRepositoryPort invoiceRepository;

    public UpdateInvoiceTotalAmount(InvoiceRepositoryPort invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    /**
     * Update the total amount for an invoice.
     *
     * @param invoiceId the ID of the invoice
     * @param newTotalAmount the new total amount
     * @return Optional containing the updated invoice, or empty if not found
     * @throws IllegalStateException if useAbsoluteValue is not enabled for this invoice
     */
    public Optional<Invoice> execute(Long invoiceId, BigDecimal newTotalAmount) {
        Optional<Invoice> invoiceOpt = invoiceRepository.findById(invoiceId);

        if (invoiceOpt.isEmpty()) {
            return Optional.empty();
        }

        Invoice invoice = invoiceOpt.get();

        if (!invoice.isUseAbsoluteValue()) {
            throw new IllegalStateException(
                "Cannot manually update total amount when useAbsoluteValue is disabled. " +
                "Enable absolute value mode first or let the system calculate from installments."
            );
        }

        invoice.setTotalAmount(newTotalAmount);

        Invoice updated = invoiceRepository.save(invoice);
        return Optional.of(updated);
    }
}
