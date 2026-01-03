package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.domain.entity.Invoice;
import com.truebalance.truebalance.domain.port.InvoiceRepositoryPort;

import java.util.Optional;

public class MarkInvoiceAsUnpaid {

    private final InvoiceRepositoryPort invoiceRepository;

    public MarkInvoiceAsUnpaid(InvoiceRepositoryPort invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public Optional<Invoice> execute(Long invoiceId) {
        Optional<Invoice> invoiceOpt = invoiceRepository.findById(invoiceId);

        if (invoiceOpt.isEmpty()) {
            return Optional.empty();
        }

        Invoice invoice = invoiceOpt.get();
        invoice.setPaid(false);
        
        return Optional.of(invoiceRepository.save(invoice));
    }
}
