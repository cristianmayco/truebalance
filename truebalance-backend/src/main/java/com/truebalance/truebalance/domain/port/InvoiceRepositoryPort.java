package com.truebalance.truebalance.domain.port;

import com.truebalance.truebalance.domain.entity.Invoice;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepositoryPort {

    Invoice save(Invoice invoice);

    List<Invoice> saveAll(List<Invoice> invoices);

    Optional<Invoice> findById(Long id);

    Optional<Invoice> findByCreditCardIdAndReferenceMonth(Long creditCardId, LocalDate referenceMonth);

    List<Invoice> findByCreditCardId(Long creditCardId);

    List<Invoice> findByCreditCardIdAndClosed(Long creditCardId, boolean closed);

    List<Invoice> findByCreditCardIdAndClosedAndPaid(Long creditCardId, boolean closed, boolean paid);

    List<Invoice> findByCreditCardIdAndRegisterAvailableLimitOrderByReferenceMonthDesc(Long creditCardId, boolean registerAvailableLimit);

    List<Invoice> findAll();
}
