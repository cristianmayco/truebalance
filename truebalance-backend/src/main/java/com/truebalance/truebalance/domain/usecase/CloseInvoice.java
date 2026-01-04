package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.domain.entity.Invoice;
import com.truebalance.truebalance.domain.port.InvoiceRepositoryPort;
import com.truebalance.truebalance.domain.port.PartialPaymentRepositoryPort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public class CloseInvoice {

    private final InvoiceRepositoryPort invoiceRepository;
    private final PartialPaymentRepositoryPort partialPaymentRepository;

    public CloseInvoice(InvoiceRepositoryPort invoiceRepository,
                        PartialPaymentRepositoryPort partialPaymentRepository) {
        this.invoiceRepository = invoiceRepository;
        this.partialPaymentRepository = partialPaymentRepository;
    }

    public Optional<Invoice> execute(Long invoiceId) {
        Optional<Invoice> invoiceOpt = invoiceRepository.findById(invoiceId);

        if (invoiceOpt.isEmpty()) {
            return Optional.empty();
        }

        Invoice invoice = invoiceOpt.get();

        // BR-I-006: Prevent closing already closed invoice
        if (invoice.isClosed()) {
            throw new IllegalStateException("Invoice is already closed");
        }

        // Step 1: Calculate partial payments total
        BigDecimal partialPaymentsTotal = partialPaymentRepository.sumByInvoiceId(invoiceId);

        // Step 2: Calculate final amount = totalAmount - partialPaymentsTotal
        // BR-I-012: Consider partial payments when closing
        BigDecimal finalAmount = invoice.getTotalAmount().subtract(partialPaymentsTotal);

        // Step 3: Determine payment status and handle negative balance
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            // finalAmount < 0: Mark as paid, transfer credit to next invoice
            invoice.setPaid(true);

            // BR-I-016: Transfer negative balance to next invoice
            LocalDate nextMonth = invoice.getReferenceMonth().plusMonths(1);
            Optional<Invoice> nextInvoiceOpt = invoiceRepository
                    .findByCreditCardIdAndReferenceMonth(invoice.getCreditCardId(), nextMonth);

            if (nextInvoiceOpt.isPresent()) {
                Invoice nextInvoice = nextInvoiceOpt.get();
                // Add negative amount as credit (previousBalance becomes negative)
                nextInvoice.setPreviousBalance(
                        nextInvoice.getPreviousBalance().add(finalAmount)
                );
                invoiceRepository.save(nextInvoice);
            } else {
                // Create next month's invoice with credit
                Invoice nextInvoice = new Invoice();
                nextInvoice.setCreditCardId(invoice.getCreditCardId());
                nextInvoice.setReferenceMonth(nextMonth);
                nextInvoice.setTotalAmount(BigDecimal.ZERO);
                nextInvoice.setPreviousBalance(finalAmount); // negative (credit)
                nextInvoice.setClosed(false);
                nextInvoice.setPaid(false);
                nextInvoice.setUseAbsoluteValue(false); // Default: calculate from installments
                invoiceRepository.save(nextInvoice);
            }
        } else if (finalAmount.compareTo(BigDecimal.ZERO) == 0) {
            // finalAmount == 0: Mark as paid
            invoice.setPaid(true);
        } else {
            // finalAmount > 0: Keep as unpaid and transfer balance to next month
            invoice.setPaid(false);

            // BR-I-017: Transfer unpaid balance to next invoice
            LocalDate nextMonth = invoice.getReferenceMonth().plusMonths(1);
            Optional<Invoice> nextInvoiceOpt = invoiceRepository
                    .findByCreditCardIdAndReferenceMonth(invoice.getCreditCardId(), nextMonth);

            if (nextInvoiceOpt.isPresent()) {
                Invoice nextInvoice = nextInvoiceOpt.get();
                // Add unpaid amount to previousBalance
                nextInvoice.setPreviousBalance(
                        nextInvoice.getPreviousBalance().add(finalAmount)
                );
                invoiceRepository.save(nextInvoice);
            } else {
                // Create next month's invoice with unpaid balance
                Invoice nextInvoice = new Invoice();
                nextInvoice.setCreditCardId(invoice.getCreditCardId());
                nextInvoice.setReferenceMonth(nextMonth);
                nextInvoice.setTotalAmount(BigDecimal.ZERO);
                nextInvoice.setPreviousBalance(finalAmount); // positive (unpaid balance)
                nextInvoice.setClosed(false);
                nextInvoice.setPaid(false);
                nextInvoice.setUseAbsoluteValue(false); // Default: calculate from installments
                invoiceRepository.save(nextInvoice);
            }
        }

        // Step 4: Mark as closed
        invoice.setClosed(true);

        // Step 5: Save and return
        Invoice closedInvoice = invoiceRepository.save(invoice);
        return Optional.of(closedInvoice);
    }
}
