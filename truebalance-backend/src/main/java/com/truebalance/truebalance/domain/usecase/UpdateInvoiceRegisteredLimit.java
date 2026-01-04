package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.domain.entity.Invoice;
import com.truebalance.truebalance.domain.port.InvoiceRepositoryPort;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Use case to update the registered available limit for an invoice.
 *
 * When registerAvailableLimit = true:
 * - This invoice becomes the starting point for limit calculations
 * - All previous invoices are ignored in limit calculations
 * - The registeredAvailableLimit value is used as the available limit for this invoice
 *
 * This feature can only be used for closed invoices.
 */
public class UpdateInvoiceRegisteredLimit {

    private final InvoiceRepositoryPort invoiceRepository;

    public UpdateInvoiceRegisteredLimit(InvoiceRepositoryPort invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    /**
     * Update the registered limit settings for an invoice.
     *
     * @param invoiceId the ID of the invoice
     * @param registerAvailableLimit whether to register a specific available limit
     * @param registeredAvailableLimit the available limit value (required when registerAvailableLimit = true)
     * @return Optional containing the updated invoice, or empty if not found
     * @throws IllegalStateException if trying to register limit on an open invoice
     * @throws IllegalArgumentException if registerAvailableLimit is true but registeredAvailableLimit is null
     */
    public Optional<Invoice> execute(Long invoiceId, boolean registerAvailableLimit, BigDecimal registeredAvailableLimit) {
        Optional<Invoice> invoiceOpt = invoiceRepository.findById(invoiceId);

        if (invoiceOpt.isEmpty()) {
            return Optional.empty();
        }

        Invoice invoice = invoiceOpt.get();

        // Validation: Only allow registering limit on closed invoices
        if (registerAvailableLimit && !invoice.isClosed()) {
            throw new IllegalStateException(
                "Cannot register available limit on an open invoice. " +
                "Please close the invoice first before registering a limit."
            );
        }

        // Validation: registeredAvailableLimit must be provided when registerAvailableLimit is true
        if (registerAvailableLimit && registeredAvailableLimit == null) {
            throw new IllegalArgumentException(
                "Registered available limit value is required when registerAvailableLimit is true"
            );
        }

        // Validation: registeredAvailableLimit must be >= 0
        if (registerAvailableLimit && registeredAvailableLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                "Registered available limit must be greater than or equal to zero"
            );
        }

        invoice.setRegisterAvailableLimit(registerAvailableLimit);
        invoice.setRegisteredAvailableLimit(registerAvailableLimit ? registeredAvailableLimit : null);

        Invoice updated = invoiceRepository.save(invoice);
        return Optional.of(updated);
    }
}
