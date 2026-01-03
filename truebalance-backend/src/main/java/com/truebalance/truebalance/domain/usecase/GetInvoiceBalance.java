package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.application.dto.output.InvoiceBalanceDTO;
import com.truebalance.truebalance.domain.entity.Invoice;
import com.truebalance.truebalance.domain.port.InvoiceRepositoryPort;
import com.truebalance.truebalance.domain.port.PartialPaymentRepositoryPort;

import java.math.BigDecimal;
import java.util.Optional;

public class GetInvoiceBalance {

    private final InvoiceRepositoryPort invoiceRepository;
    private final PartialPaymentRepositoryPort partialPaymentRepository;

    public GetInvoiceBalance(InvoiceRepositoryPort invoiceRepository,
                             PartialPaymentRepositoryPort partialPaymentRepository) {
        this.invoiceRepository = invoiceRepository;
        this.partialPaymentRepository = partialPaymentRepository;
    }

    public Optional<InvoiceBalanceDTO> execute(Long invoiceId) {
        Optional<Invoice> invoiceOpt = invoiceRepository.findById(invoiceId);

        if (invoiceOpt.isEmpty()) {
            return Optional.empty();
        }

        Invoice invoice = invoiceOpt.get();

        // Calculate partial payments total
        BigDecimal partialPaymentsTotal = partialPaymentRepository.sumByInvoiceId(invoiceId);
        int partialPaymentsCount = partialPaymentRepository.countByInvoiceId(invoiceId);

        // BR-I-011: Calculate current balance
        // currentBalance = totalAmount + previousBalance - partialPaymentsTotal
        BigDecimal currentBalance = invoice.getTotalAmount()
                .add(invoice.getPreviousBalance())
                .subtract(partialPaymentsTotal);

        InvoiceBalanceDTO balanceDTO = new InvoiceBalanceDTO(
                invoice.getId(),
                invoice.getTotalAmount(),
                invoice.getPreviousBalance(),
                partialPaymentsTotal,
                currentBalance,
                invoice.isPaid(),
                invoice.isClosed(),
                partialPaymentsCount
        );

        return Optional.of(balanceDTO);
    }
}
