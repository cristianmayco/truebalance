package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.domain.entity.Invoice;
import com.truebalance.truebalance.domain.entity.PartialPayment;
import com.truebalance.truebalance.domain.exception.InvoiceClosedException;
import com.truebalance.truebalance.domain.exception.PartialPaymentNotFoundException;
import com.truebalance.truebalance.domain.port.InvoiceRepositoryPort;
import com.truebalance.truebalance.domain.port.PartialPaymentRepositoryPort;

import java.util.Optional;

/**
 * Use case to delete a partial payment from an open invoice.
 * BR-PP-003: Partial payments can only be deleted if invoice is open (closed = false)
 * BR-PP-004: Partial payments are immutable (cannot be edited, only deleted)
 */
public class DeletePartialPayment {

    private final PartialPaymentRepositoryPort partialPaymentRepository;
    private final InvoiceRepositoryPort invoiceRepository;

    public DeletePartialPayment(PartialPaymentRepositoryPort partialPaymentRepository,
                                InvoiceRepositoryPort invoiceRepository) {
        this.partialPaymentRepository = partialPaymentRepository;
        this.invoiceRepository = invoiceRepository;
    }

    /**
     * Execute the use case.
     * Validates that the invoice is open before allowing deletion.
     *
     * @param partialPaymentId the ID of the partial payment to delete
     * @return true if deleted successfully
     * @throws PartialPaymentNotFoundException if payment not found
     * @throws InvoiceClosedException if invoice is closed (BR-PP-003)
     */
    public boolean execute(Long partialPaymentId) {
        // Find the partial payment
        Optional<PartialPayment> partialPaymentOpt = partialPaymentRepository.findById(partialPaymentId);

        if (partialPaymentOpt.isEmpty()) {
            throw new PartialPaymentNotFoundException(partialPaymentId);
        }

        PartialPayment partialPayment = partialPaymentOpt.get();

        // Find the associated invoice
        Optional<Invoice> invoiceOpt = invoiceRepository.findById(partialPayment.getInvoiceId());

        if (invoiceOpt.isEmpty()) {
            // This should not happen in normal flow, but handle gracefully
            throw new IllegalStateException("Invoice not found for partial payment ID: " + partialPaymentId);
        }

        Invoice invoice = invoiceOpt.get();

        // BR-PP-003: Validate invoice is open
        if (invoice.isClosed()) {
            throw new InvoiceClosedException(invoice.getId());
        }

        // Delete the partial payment
        partialPaymentRepository.deleteById(partialPaymentId);
        return true;
    }
}
