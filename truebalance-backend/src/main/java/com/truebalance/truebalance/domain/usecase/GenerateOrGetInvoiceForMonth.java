package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.domain.entity.Invoice;
import com.truebalance.truebalance.domain.port.InvoiceRepositoryPort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public class GenerateOrGetInvoiceForMonth {

    private final InvoiceRepositoryPort invoiceRepository;

    public GenerateOrGetInvoiceForMonth(InvoiceRepositoryPort invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public Invoice execute(Long creditCardId, LocalDate referenceMonth) {
        // BR-I-002: One invoice per card per month
        Optional<Invoice> existing = invoiceRepository
                .findByCreditCardIdAndReferenceMonth(creditCardId, referenceMonth);

        if (existing.isPresent()) {
            return existing.get();
        }

        // BR-I-001: Create new invoice
        Invoice newInvoice = new Invoice();
        newInvoice.setCreditCardId(creditCardId);
        newInvoice.setReferenceMonth(referenceMonth);
        newInvoice.setTotalAmount(BigDecimal.ZERO);
        newInvoice.setPreviousBalance(BigDecimal.ZERO);
        newInvoice.setClosed(false);
        newInvoice.setPaid(false);
        newInvoice.setUseAbsoluteValue(false); // Default: calculate from installments

        return invoiceRepository.save(newInvoice);
    }
}
