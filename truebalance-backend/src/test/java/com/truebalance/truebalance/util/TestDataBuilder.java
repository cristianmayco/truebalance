package com.truebalance.truebalance.util;

import com.truebalance.truebalance.domain.entity.*;
import com.truebalance.truebalance.domain.usecase.AvailableLimitResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Test data builder utility class for creating domain entities with sensible defaults.
 * All methods return fully initialized objects ready for testing.
 */
public class TestDataBuilder {

    /**
     * Creates a Bill with default values.
     * Default: 1000.00 total, 10 installments of 100.00 each
     */
    public static Bill createBill() {
        Bill bill = new Bill();
        bill.setId(1L);
        bill.setName("Test Bill");
        bill.setExecutionDate(LocalDateTime.of(2025, 1, 15, 10, 0));
        bill.setTotalAmount(new BigDecimal("1000.00"));
        bill.setNumberOfInstallments(10);
        bill.setInstallmentAmount(new BigDecimal("100.00"));
        bill.setDescription("Test bill description");
        bill.setIsRecurring(false);
        bill.setCategory("Teste");
        bill.setCreatedAt(LocalDateTime.of(2025, 1, 15, 10, 0));
        bill.setUpdatedAt(LocalDateTime.of(2025, 1, 15, 10, 0));
        return bill;
    }

    /**
     * Creates a Bill with custom parameters.
     */
    public static Bill createBill(Long id, String name, BigDecimal totalAmount, int numberOfInstallments) {
        Bill bill = createBill();
        bill.setId(id);
        bill.setName(name);
        bill.setTotalAmount(totalAmount);
        bill.setNumberOfInstallments(numberOfInstallments);
        bill.setInstallmentAmount(totalAmount.divide(BigDecimal.valueOf(numberOfInstallments), 2, java.math.RoundingMode.HALF_UP));
        return bill;
    }

    /**
     * Creates a CreditCard with default values.
     * Default: 5000.00 limit, closing day 10, due day 17
     */
    public static CreditCard createCreditCard() {
        CreditCard card = new CreditCard();
        card.setId(1L);
        card.setName("Test Credit Card");
        card.setCreditLimit(new BigDecimal("5000.00"));
        card.setClosingDay(10);
        card.setDueDay(17);
        card.setAllowsPartialPayment(true);
        card.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        card.setUpdatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        return card;
    }

    /**
     * Creates a CreditCard with custom parameters.
     */
    public static CreditCard createCreditCard(Long id, String name, BigDecimal creditLimit, int closingDay, int dueDay) {
        CreditCard card = createCreditCard();
        card.setId(id);
        card.setName(name);
        card.setCreditLimit(creditLimit);
        card.setClosingDay(closingDay);
        card.setDueDay(dueDay);
        return card;
    }

    /**
     * Creates an Invoice with default values.
     * Default: January 2025, 500.00 total, open (not closed/paid)
     */
    public static Invoice createInvoice() {
        Invoice invoice = new Invoice();
        invoice.setId(1L);
        invoice.setCreditCardId(1L);
        invoice.setReferenceMonth(LocalDate.of(2025, 1, 1));
        invoice.setTotalAmount(new BigDecimal("500.00"));
        invoice.setPreviousBalance(BigDecimal.ZERO);
        invoice.setClosed(false);
        invoice.setPaid(false);
        invoice.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        invoice.setUpdatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        return invoice;
    }

    /**
     * Creates an Invoice with custom parameters.
     */
    public static Invoice createInvoice(Long id, Long creditCardId, LocalDate referenceMonth, BigDecimal totalAmount) {
        Invoice invoice = createInvoice();
        invoice.setId(id);
        invoice.setCreditCardId(creditCardId);
        invoice.setReferenceMonth(referenceMonth);
        invoice.setTotalAmount(totalAmount);
        return invoice;
    }

    /**
     * Creates an Installment with default values.
     * Default: 100.00 amount, installment 1 of 10, due January 17, 2025
     */
    public static Installment createInstallment() {
        Installment installment = new Installment();
        installment.setId(1L);
        installment.setBillId(1L);
        installment.setCreditCardId(1L);
        installment.setInvoiceId(1L);
        installment.setInstallmentNumber(1);
        installment.setAmount(new BigDecimal("100.00"));
        installment.setDueDate(LocalDate.of(2025, 1, 17));
        installment.setCreatedAt(LocalDateTime.of(2025, 1, 15, 10, 0));
        return installment;
    }

    /**
     * Creates an Installment with custom parameters.
     */
    public static Installment createInstallment(Long id, Long billId, Long invoiceId, int installmentNumber, BigDecimal amount, LocalDate dueDate) {
        Installment installment = createInstallment();
        installment.setId(id);
        installment.setBillId(billId);
        installment.setInvoiceId(invoiceId);
        installment.setInstallmentNumber(installmentNumber);
        installment.setAmount(amount);
        installment.setDueDate(dueDate);
        return installment;
    }

    /**
     * Creates a PartialPayment with default values.
     * Default: 200.00 amount, paid on January 15, 2025
     */
    public static PartialPayment createPartialPayment() {
        PartialPayment payment = new PartialPayment();
        payment.setId(1L);
        payment.setInvoiceId(1L);
        payment.setAmount(new BigDecimal("200.00"));
        payment.setPaymentDate(LocalDateTime.of(2025, 1, 15, 14, 30));
        payment.setDescription("Test partial payment");
        payment.setCreatedAt(LocalDateTime.of(2025, 1, 15, 14, 30));
        return payment;
    }

    /**
     * Creates a PartialPayment with custom parameters.
     */
    public static PartialPayment createPartialPayment(Long id, Long invoiceId, BigDecimal amount, LocalDateTime paymentDate) {
        PartialPayment payment = createPartialPayment();
        payment.setId(id);
        payment.setInvoiceId(invoiceId);
        payment.setAmount(amount);
        payment.setPaymentDate(paymentDate);
        return payment;
    }

    /**
     * Creates an AvailableLimitResult with default values.
     * Default: 5000.00 credit limit, 0.00 used, 0.00 partial payments, 5000.00 available
     */
    public static AvailableLimitResult createAvailableLimitResult() {
        return new AvailableLimitResult(
                1L,
                new BigDecimal("5000.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("5000.00")
        );
    }

    /**
     * Creates an AvailableLimitResult with custom parameters.
     */
    public static AvailableLimitResult createAvailableLimitResult(
            Long creditCardId,
            BigDecimal creditLimit,
            BigDecimal usedLimit,
            BigDecimal partialPaymentsTotal,
            BigDecimal availableLimit) {
        return new AvailableLimitResult(
                creditCardId,
                creditLimit,
                usedLimit,
                partialPaymentsTotal,
                availableLimit
        );
    }

    /**
     * Creates an InstallmentDateInfo with default values.
     * Default: Installment 1, due January 17, 2025, reference month January 2025
     */
    public static InstallmentDateInfo createInstallmentDateInfo() {
        return new InstallmentDateInfo(
                1,
                LocalDate.of(2025, 1, 17),
                LocalDate.of(2025, 1, 1)
        );
    }

    /**
     * Creates an InstallmentDateInfo with custom parameters.
     */
    public static InstallmentDateInfo createInstallmentDateInfo(int installmentNumber, LocalDate dueDate, LocalDate referenceMonth) {
        return new InstallmentDateInfo(installmentNumber, dueDate, referenceMonth);
    }
}
