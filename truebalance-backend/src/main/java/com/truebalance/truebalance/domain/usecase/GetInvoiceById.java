package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.domain.entity.Invoice;
import com.truebalance.truebalance.domain.port.InvoiceRepositoryPort;

import java.util.Optional;

public class GetInvoiceById {

    private final InvoiceRepositoryPort invoiceRepository;

    public GetInvoiceById(InvoiceRepositoryPort invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public Optional<Invoice> execute(Long id) {
        return invoiceRepository.findById(id);
    }
}
