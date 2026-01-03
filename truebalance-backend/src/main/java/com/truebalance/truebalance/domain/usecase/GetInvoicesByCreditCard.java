package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.domain.entity.Invoice;
import com.truebalance.truebalance.domain.port.InvoiceRepositoryPort;

import java.util.List;

public class GetInvoicesByCreditCard {

    private final InvoiceRepositoryPort invoiceRepository;

    public GetInvoicesByCreditCard(InvoiceRepositoryPort invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public List<Invoice> execute(Long creditCardId) {
        return invoiceRepository.findByCreditCardId(creditCardId);
    }
}
