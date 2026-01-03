package com.truebalance.truebalance.domain.usecase.bill;

import com.truebalance.truebalance.domain.entity.*;
import com.truebalance.truebalance.domain.exception.CreditCardNotFoundException;
import com.truebalance.truebalance.domain.exception.CreditLimitExceededException;
import com.truebalance.truebalance.domain.port.CreditCardRepositoryPort;
import com.truebalance.truebalance.domain.port.InstallmentRepositoryPort;
import com.truebalance.truebalance.domain.port.InvoiceRepositoryPort;
import com.truebalance.truebalance.domain.service.InstallmentDateCalculator;
import com.truebalance.truebalance.domain.usecase.AvailableLimitResult;
import com.truebalance.truebalance.domain.usecase.CreateBill;
import com.truebalance.truebalance.domain.usecase.CreateBillWithCreditCard;
import com.truebalance.truebalance.domain.usecase.GenerateOrGetInvoiceForMonth;
import com.truebalance.truebalance.domain.usecase.GetAvailableLimit;
import com.truebalance.truebalance.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for CreateBillWithCreditCard use case.
 *
 * This is the MOST CRITICAL use case in the system as it integrates:
 * - Bill creation
 * - Credit card validation and limit checking
 * - Invoice generation/retrieval
 * - Installment distribution across invoices
 * - Batch updates for performance
 *
 * Business Rules Tested:
 * - BR-B-004: Distribution of installments across invoices
 * - BR-CC-008: Validation of available credit limit
 * - BR-I-001: Invoice creation
 * - BR-I-002: One invoice per credit card per month
 * - BR-I-004: Billing cycle calculation
 * - BR-I-005: Calculation of invoice total amount
 * - BR-INS-001: Installment creation
 * - BR-INS-002: Installment sequencing
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CreateBillWithCreditCard - Critical Use Case Tests")
class CreateBillWithCreditCardTest {

    @Mock
    private CreateBill createBill;

    @Mock
    private CreditCardRepositoryPort creditCardRepository;

    @Mock
    private InstallmentRepositoryPort installmentRepository;

    @Mock
    private InvoiceRepositoryPort invoiceRepository;

    @Mock
    private GenerateOrGetInvoiceForMonth generateOrGetInvoiceForMonth;

    @Mock
    private GetAvailableLimit getAvailableLimit;

    @Mock
    private InstallmentDateCalculator installmentDateCalculator;

    @Captor
    private ArgumentCaptor<List<Invoice>> invoiceListCaptor;

    @Captor
    private ArgumentCaptor<List<Installment>> installmentListCaptor;

    private CreateBillWithCreditCard useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateBillWithCreditCard(
                createBill,
                creditCardRepository,
                installmentRepository,
                invoiceRepository,
                generateOrGetInvoiceForMonth,
                getAvailableLimit,
                installmentDateCalculator
        );
    }

    // ========== HAPPY PATH TESTS ==========

    @Test
    @DisplayName("Should create bill with single installment successfully")
    void shouldCreateBillWithSingleInstallment() {
        // Given: Bill with 1 installment
        Bill inputBill = TestDataBuilder.createBill(null, "Single Installment Bill", new BigDecimal("500.00"), 1);
        Bill savedBill = TestDataBuilder.createBill(1L, "Single Installment Bill", new BigDecimal("500.00"), 1);
        Long creditCardId = 1L;
        CreditCard creditCard = TestDataBuilder.createCreditCard(creditCardId, "Test Card", new BigDecimal("5000.00"), 10, 17);
        AvailableLimitResult limitResult = TestDataBuilder.createAvailableLimitResult(creditCardId, new BigDecimal("5000.00"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("5000.00"));

        Invoice invoice = TestDataBuilder.createInvoice(1L, creditCardId, LocalDate.of(2025, 1, 1), BigDecimal.ZERO);
        InstallmentDateInfo dateInfo = TestDataBuilder.createInstallmentDateInfo(1, LocalDate.of(2025, 1, 17), LocalDate.of(2025, 1, 1));

        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(getAvailableLimit.execute(creditCardId)).thenReturn(limitResult);
        when(createBill.addBill(inputBill)).thenReturn(savedBill);
        when(installmentDateCalculator.calculate(any(), eq(10), eq(17), eq(1))).thenReturn(dateInfo);
        when(generateOrGetInvoiceForMonth.execute(creditCardId, LocalDate.of(2025, 1, 1))).thenReturn(invoice);
        when(invoiceRepository.saveAll(anyList())).thenReturn(List.of(invoice));
        when(installmentRepository.saveAll(anyList())).thenReturn(List.of());

        // When
        Bill result = useCase.execute(inputBill, creditCardId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(creditCardRepository).findById(creditCardId);
        verify(getAvailableLimit).execute(creditCardId);
        verify(createBill).addBill(inputBill);
        verify(installmentDateCalculator).calculate(any(), eq(10), eq(17), eq(1));
        verify(invoiceRepository).saveAll(invoiceListCaptor.capture());
        verify(installmentRepository).saveAll(installmentListCaptor.capture());

        List<Installment> savedInstallments = installmentListCaptor.getValue();
        assertThat(savedInstallments).hasSize(1);
        assertThat(savedInstallments.get(0).getInstallmentNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should create bill with multiple installments in same month")
    void shouldCreateBillWithMultipleInstallmentsSameMonth() {
        // Given: Bill with 3 installments, all in January 2025
        Bill inputBill = TestDataBuilder.createBill(null, "Multi Installment Bill", new BigDecimal("300.00"), 3);
        Bill savedBill = TestDataBuilder.createBill(1L, "Multi Installment Bill", new BigDecimal("300.00"), 3);
        savedBill.setInstallmentAmount(new BigDecimal("100.00"));
        Long creditCardId = 1L;
        CreditCard creditCard = TestDataBuilder.createCreditCard(creditCardId, "Test Card", new BigDecimal("5000.00"), 10, 17);
        AvailableLimitResult limitResult = TestDataBuilder.createAvailableLimitResult();

        Invoice invoice = TestDataBuilder.createInvoice(1L, creditCardId, LocalDate.of(2025, 1, 1), BigDecimal.ZERO);

        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(getAvailableLimit.execute(creditCardId)).thenReturn(limitResult);
        when(createBill.addBill(inputBill)).thenReturn(savedBill);
        when(generateOrGetInvoiceForMonth.execute(creditCardId, LocalDate.of(2025, 1, 1))).thenReturn(invoice);

        // All installments in same month
        when(installmentDateCalculator.calculate(any(), eq(10), eq(17), eq(1)))
                .thenReturn(TestDataBuilder.createInstallmentDateInfo(1, LocalDate.of(2025, 1, 17), LocalDate.of(2025, 1, 1)));
        when(installmentDateCalculator.calculate(any(), eq(10), eq(17), eq(2)))
                .thenReturn(TestDataBuilder.createInstallmentDateInfo(2, LocalDate.of(2025, 1, 17), LocalDate.of(2025, 1, 1)));
        when(installmentDateCalculator.calculate(any(), eq(10), eq(17), eq(3)))
                .thenReturn(TestDataBuilder.createInstallmentDateInfo(3, LocalDate.of(2025, 1, 17), LocalDate.of(2025, 1, 1)));

        when(invoiceRepository.saveAll(anyList())).thenReturn(List.of(invoice));
        when(installmentRepository.saveAll(anyList())).thenReturn(List.of());

        // When
        Bill result = useCase.execute(inputBill, creditCardId);

        // Then
        assertThat(result).isNotNull();
        verify(installmentRepository).saveAll(installmentListCaptor.capture());

        List<Installment> savedInstallments = installmentListCaptor.getValue();
        assertThat(savedInstallments).hasSize(3);
        assertThat(savedInstallments).extracting(Installment::getInstallmentNumber).containsExactly(1, 2, 3);
        assertThat(savedInstallments).allMatch(i -> i.getInvoiceId().equals(1L));

        // Verify invoice was fetched only once (cache working)
        verify(generateOrGetInvoiceForMonth, times(1)).execute(creditCardId, LocalDate.of(2025, 1, 1));
    }

    @Test
    @DisplayName("Should create bill with installments across multiple months")
    void shouldCreateBillWithInstallmentsAcrossMultipleMonths() {
        // Given: Bill with 3 installments across different months
        Bill inputBill = TestDataBuilder.createBill(null, "Cross-Month Bill", new BigDecimal("300.00"), 3);
        Bill savedBill = TestDataBuilder.createBill(1L, "Cross-Month Bill", new BigDecimal("300.00"), 3);
        savedBill.setInstallmentAmount(new BigDecimal("100.00"));
        Long creditCardId = 1L;
        CreditCard creditCard = TestDataBuilder.createCreditCard(creditCardId, "Test Card", new BigDecimal("5000.00"), 10, 17);
        AvailableLimitResult limitResult = TestDataBuilder.createAvailableLimitResult();

        Invoice invoice1 = TestDataBuilder.createInvoice(1L, creditCardId, LocalDate.of(2025, 1, 1), BigDecimal.ZERO);
        Invoice invoice2 = TestDataBuilder.createInvoice(2L, creditCardId, LocalDate.of(2025, 2, 1), BigDecimal.ZERO);
        Invoice invoice3 = TestDataBuilder.createInvoice(3L, creditCardId, LocalDate.of(2025, 3, 1), BigDecimal.ZERO);

        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(getAvailableLimit.execute(creditCardId)).thenReturn(limitResult);
        when(createBill.addBill(inputBill)).thenReturn(savedBill);

        // Installments in different months
        when(installmentDateCalculator.calculate(any(), eq(10), eq(17), eq(1)))
                .thenReturn(TestDataBuilder.createInstallmentDateInfo(1, LocalDate.of(2025, 1, 17), LocalDate.of(2025, 1, 1)));
        when(installmentDateCalculator.calculate(any(), eq(10), eq(17), eq(2)))
                .thenReturn(TestDataBuilder.createInstallmentDateInfo(2, LocalDate.of(2025, 2, 17), LocalDate.of(2025, 2, 1)));
        when(installmentDateCalculator.calculate(any(), eq(10), eq(17), eq(3)))
                .thenReturn(TestDataBuilder.createInstallmentDateInfo(3, LocalDate.of(2025, 3, 17), LocalDate.of(2025, 3, 1)));

        when(generateOrGetInvoiceForMonth.execute(creditCardId, LocalDate.of(2025, 1, 1))).thenReturn(invoice1);
        when(generateOrGetInvoiceForMonth.execute(creditCardId, LocalDate.of(2025, 2, 1))).thenReturn(invoice2);
        when(generateOrGetInvoiceForMonth.execute(creditCardId, LocalDate.of(2025, 3, 1))).thenReturn(invoice3);

        when(invoiceRepository.saveAll(anyList())).thenReturn(List.of(invoice1, invoice2, invoice3));
        when(installmentRepository.saveAll(anyList())).thenReturn(List.of());

        // When
        Bill result = useCase.execute(inputBill, creditCardId);

        // Then
        assertThat(result).isNotNull();
        verify(invoiceRepository).saveAll(invoiceListCaptor.capture());
        verify(installmentRepository).saveAll(installmentListCaptor.capture());

        List<Invoice> savedInvoices = invoiceListCaptor.getValue();
        assertThat(savedInvoices).hasSize(3);

        List<Installment> savedInstallments = installmentListCaptor.getValue();
        assertThat(savedInstallments).hasSize(3);
        assertThat(savedInstallments.get(0).getInvoiceId()).isEqualTo(1L);
        assertThat(savedInstallments.get(1).getInvoiceId()).isEqualTo(2L);
        assertThat(savedInstallments.get(2).getInvoiceId()).isEqualTo(3L);
    }

    @Test
    @DisplayName("Should use existing invoices when available")
    void shouldUseExistingInvoicesWhenAvailable() {
        // Given: Existing invoice for the month
        Bill inputBill = TestDataBuilder.createBill(null, "Bill", new BigDecimal("200.00"), 2);
        Bill savedBill = TestDataBuilder.createBill(1L, "Bill", new BigDecimal("200.00"), 2);
        savedBill.setInstallmentAmount(new BigDecimal("100.00"));
        Long creditCardId = 1L;
        CreditCard creditCard = TestDataBuilder.createCreditCard();
        AvailableLimitResult limitResult = TestDataBuilder.createAvailableLimitResult();

        // Existing invoice with previous total amount
        Invoice existingInvoice = TestDataBuilder.createInvoice(1L, creditCardId, LocalDate.of(2025, 1, 1), new BigDecimal("500.00"));

        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(getAvailableLimit.execute(creditCardId)).thenReturn(limitResult);
        when(createBill.addBill(inputBill)).thenReturn(savedBill);
        when(generateOrGetInvoiceForMonth.execute(creditCardId, LocalDate.of(2025, 1, 1))).thenReturn(existingInvoice);

        when(installmentDateCalculator.calculate(any(), anyInt(), anyInt(), anyInt()))
                .thenReturn(TestDataBuilder.createInstallmentDateInfo(1, LocalDate.of(2025, 1, 17), LocalDate.of(2025, 1, 1)));

        when(invoiceRepository.saveAll(anyList())).thenReturn(List.of(existingInvoice));
        when(installmentRepository.saveAll(anyList())).thenReturn(List.of());

        // When
        useCase.execute(inputBill, creditCardId);

        // Then
        verify(invoiceRepository).saveAll(invoiceListCaptor.capture());
        List<Invoice> savedInvoices = invoiceListCaptor.getValue();

        // Verify invoice total was updated correctly (500.00 + 100.00 + 100.00 = 700.00)
        assertThat(savedInvoices.get(0).getTotalAmount()).isEqualByComparingTo("700.00");
    }

    @Test
    @DisplayName("Should handle bill with 12 installments (yearly)")
    void shouldHandleBillWith12Installments() {
        // Given: Bill with 12 installments
        Bill inputBill = TestDataBuilder.createBill(null, "Yearly Bill", new BigDecimal("1200.00"), 12);
        Bill savedBill = TestDataBuilder.createBill(1L, "Yearly Bill", new BigDecimal("1200.00"), 12);
        savedBill.setInstallmentAmount(new BigDecimal("100.00"));
        Long creditCardId = 1L;
        CreditCard creditCard = TestDataBuilder.createCreditCard();
        AvailableLimitResult limitResult = TestDataBuilder.createAvailableLimitResult(
                creditCardId, new BigDecimal("5000.00"), new BigDecimal("0.00"), BigDecimal.ZERO, new BigDecimal("5000.00")
        );

        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(getAvailableLimit.execute(creditCardId)).thenReturn(limitResult);
        when(createBill.addBill(inputBill)).thenReturn(savedBill);

        // Mock 12 different months
        for (int i = 1; i <= 12; i++) {
            LocalDate referenceMonth = LocalDate.of(2025, i, 1);
            LocalDate dueDate = LocalDate.of(2025, i, 17);
            when(installmentDateCalculator.calculate(any(), anyInt(), anyInt(), eq(i)))
                    .thenReturn(TestDataBuilder.createInstallmentDateInfo(i, dueDate, referenceMonth));

            Invoice invoice = TestDataBuilder.createInvoice((long) i, creditCardId, referenceMonth, BigDecimal.ZERO);
            when(generateOrGetInvoiceForMonth.execute(creditCardId, referenceMonth)).thenReturn(invoice);
        }

        when(invoiceRepository.saveAll(anyList())).thenReturn(List.of());
        when(installmentRepository.saveAll(anyList())).thenReturn(List.of());

        // When
        Bill result = useCase.execute(inputBill, creditCardId);

        // Then
        assertThat(result).isNotNull();
        verify(installmentRepository).saveAll(installmentListCaptor.capture());

        List<Installment> savedInstallments = installmentListCaptor.getValue();
        assertThat(savedInstallments).hasSize(12);
        assertThat(savedInstallments).extracting(Installment::getInstallmentNumber)
                .containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
    }

    // ========== VALIDATION TESTS ==========

    @Test
    @DisplayName("Should throw exception when credit card not found")
    void shouldThrowExceptionWhenCreditCardNotFound() {
        // Given: Credit card does not exist
        Bill inputBill = TestDataBuilder.createBill();
        Long nonExistentCardId = 999L;

        when(creditCardRepository.findById(nonExistentCardId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> useCase.execute(inputBill, nonExistentCardId))
                .isInstanceOf(CreditCardNotFoundException.class)
                .hasMessageContaining("999");

        // Verify no further processing occurred
        verify(getAvailableLimit, never()).execute(anyLong());
        verify(createBill, never()).addBill(any());
    }

    @Test
    @DisplayName("Should throw exception when credit limit exceeded")
    void shouldThrowExceptionWhenCreditLimitExceeded() {
        // Given: Bill amount exceeds available limit
        Bill inputBill = TestDataBuilder.createBill(null, "Expensive Bill", new BigDecimal("6000.00"), 1);
        Long creditCardId = 1L;
        CreditCard creditCard = TestDataBuilder.createCreditCard(creditCardId, "Test Card", new BigDecimal("5000.00"), 10, 17);
        AvailableLimitResult limitResult = TestDataBuilder.createAvailableLimitResult(
                creditCardId, new BigDecimal("5000.00"), new BigDecimal("4000.00"), BigDecimal.ZERO, new BigDecimal("1000.00")
        );

        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(getAvailableLimit.execute(creditCardId)).thenReturn(limitResult);

        // When & Then
        assertThatThrownBy(() -> useCase.execute(inputBill, creditCardId))
                .isInstanceOf(CreditLimitExceededException.class)
                .hasMessageContaining("Limite insuficiente")
                .hasMessageContaining("6000")
                .hasMessageContaining("1000");

        // Verify bill was NOT created
        verify(createBill, never()).addBill(any());
    }

    @Test
    @DisplayName("Should allow bill when amount equals available limit (boundary)")
    void shouldAllowBillWhenAmountEqualsAvailableLimit() {
        // Given: Bill amount exactly equals available limit
        Bill inputBill = TestDataBuilder.createBill(null, "Exact Limit Bill", new BigDecimal("1000.00"), 1);
        Bill savedBill = TestDataBuilder.createBill(1L, "Exact Limit Bill", new BigDecimal("1000.00"), 1);
        Long creditCardId = 1L;
        CreditCard creditCard = TestDataBuilder.createCreditCard();
        AvailableLimitResult limitResult = TestDataBuilder.createAvailableLimitResult(
                creditCardId, new BigDecimal("5000.00"), new BigDecimal("4000.00"), BigDecimal.ZERO, new BigDecimal("1000.00")
        );

        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(getAvailableLimit.execute(creditCardId)).thenReturn(limitResult);
        when(createBill.addBill(inputBill)).thenReturn(savedBill);
        when(installmentDateCalculator.calculate(any(), anyInt(), anyInt(), anyInt()))
                .thenReturn(TestDataBuilder.createInstallmentDateInfo());
        when(generateOrGetInvoiceForMonth.execute(anyLong(), any())).thenReturn(TestDataBuilder.createInvoice());
        when(invoiceRepository.saveAll(anyList())).thenReturn(List.of());
        when(installmentRepository.saveAll(anyList())).thenReturn(List.of());

        // When
        Bill result = useCase.execute(inputBill, creditCardId);

        // Then: Should succeed
        assertThat(result).isNotNull();
        verify(createBill).addBill(inputBill);
    }

    // ========== BUSINESS RULES TESTS ==========

    @Test
    @DisplayName("BR-B-004: Should distribute installments correctly across invoices")
    void shouldDistributeInstallmentsCorrectlyAcrossInvoices() {
        // Given: Bill with installments across 3 different invoices
        Bill inputBill = TestDataBuilder.createBill(null, "Distributed Bill", new BigDecimal("450.00"), 6);
        Bill savedBill = TestDataBuilder.createBill(1L, "Distributed Bill", new BigDecimal("450.00"), 6);
        savedBill.setInstallmentAmount(new BigDecimal("75.00"));
        Long creditCardId = 1L;
        CreditCard creditCard = TestDataBuilder.createCreditCard();
        AvailableLimitResult limitResult = TestDataBuilder.createAvailableLimitResult();

        Invoice invoice1 = TestDataBuilder.createInvoice(1L, creditCardId, LocalDate.of(2025, 1, 1), BigDecimal.ZERO);
        Invoice invoice2 = TestDataBuilder.createInvoice(2L, creditCardId, LocalDate.of(2025, 2, 1), BigDecimal.ZERO);
        Invoice invoice3 = TestDataBuilder.createInvoice(3L, creditCardId, LocalDate.of(2025, 3, 1), BigDecimal.ZERO);

        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(getAvailableLimit.execute(creditCardId)).thenReturn(limitResult);
        when(createBill.addBill(inputBill)).thenReturn(savedBill);

        // 2 installments per invoice
        when(installmentDateCalculator.calculate(any(), anyInt(), anyInt(), eq(1)))
                .thenReturn(TestDataBuilder.createInstallmentDateInfo(1, LocalDate.of(2025, 1, 17), LocalDate.of(2025, 1, 1)));
        when(installmentDateCalculator.calculate(any(), anyInt(), anyInt(), eq(2)))
                .thenReturn(TestDataBuilder.createInstallmentDateInfo(2, LocalDate.of(2025, 1, 17), LocalDate.of(2025, 1, 1)));
        when(installmentDateCalculator.calculate(any(), anyInt(), anyInt(), eq(3)))
                .thenReturn(TestDataBuilder.createInstallmentDateInfo(3, LocalDate.of(2025, 2, 17), LocalDate.of(2025, 2, 1)));
        when(installmentDateCalculator.calculate(any(), anyInt(), anyInt(), eq(4)))
                .thenReturn(TestDataBuilder.createInstallmentDateInfo(4, LocalDate.of(2025, 2, 17), LocalDate.of(2025, 2, 1)));
        when(installmentDateCalculator.calculate(any(), anyInt(), anyInt(), eq(5)))
                .thenReturn(TestDataBuilder.createInstallmentDateInfo(5, LocalDate.of(2025, 3, 17), LocalDate.of(2025, 3, 1)));
        when(installmentDateCalculator.calculate(any(), anyInt(), anyInt(), eq(6)))
                .thenReturn(TestDataBuilder.createInstallmentDateInfo(6, LocalDate.of(2025, 3, 17), LocalDate.of(2025, 3, 1)));

        when(generateOrGetInvoiceForMonth.execute(creditCardId, LocalDate.of(2025, 1, 1))).thenReturn(invoice1);
        when(generateOrGetInvoiceForMonth.execute(creditCardId, LocalDate.of(2025, 2, 1))).thenReturn(invoice2);
        when(generateOrGetInvoiceForMonth.execute(creditCardId, LocalDate.of(2025, 3, 1))).thenReturn(invoice3);

        when(invoiceRepository.saveAll(anyList())).thenReturn(List.of());
        when(installmentRepository.saveAll(anyList())).thenReturn(List.of());

        // When
        useCase.execute(inputBill, creditCardId);

        // Then: Verify each invoice received correct amount (2 * 75.00 = 150.00)
        verify(invoiceRepository).saveAll(invoiceListCaptor.capture());
        List<Invoice> savedInvoices = invoiceListCaptor.getValue();

        assertThat(savedInvoices).hasSize(3);
        assertThat(savedInvoices).allMatch(inv -> inv.getTotalAmount().compareTo(new BigDecimal("150.00")) == 0);
    }

    @Test
    @DisplayName("BR-CC-008: Should validate available limit before creating bill")
    void shouldValidateAvailableLimitBeforeCreatingBill() {
        // Given
        Bill inputBill = TestDataBuilder.createBill();
        Long creditCardId = 1L;
        CreditCard creditCard = TestDataBuilder.createCreditCard();
        AvailableLimitResult limitResult = TestDataBuilder.createAvailableLimitResult();

        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(getAvailableLimit.execute(creditCardId)).thenReturn(limitResult);
        when(createBill.addBill(inputBill)).thenReturn(TestDataBuilder.createBill());
        when(installmentDateCalculator.calculate(any(), anyInt(), anyInt(), anyInt()))
                .thenReturn(TestDataBuilder.createInstallmentDateInfo());
        when(generateOrGetInvoiceForMonth.execute(anyLong(), any())).thenReturn(TestDataBuilder.createInvoice());
        when(invoiceRepository.saveAll(anyList())).thenReturn(List.of());
        when(installmentRepository.saveAll(anyList())).thenReturn(List.of());

        // When
        useCase.execute(inputBill, creditCardId);

        // Then: Verify limit check happened BEFORE bill creation
        var inOrder = inOrder(getAvailableLimit, createBill);
        inOrder.verify(getAvailableLimit).execute(creditCardId);
        inOrder.verify(createBill).addBill(inputBill);
    }

    @Test
    @DisplayName("BR-INS-001 & BR-INS-002: Should create installments with correct sequencing")
    void shouldCreateInstallmentsWithCorrectSequencing() {
        // Given: Bill with 5 installments
        Bill inputBill = TestDataBuilder.createBill(null, "Sequenced Bill", new BigDecimal("500.00"), 5);
        Bill savedBill = TestDataBuilder.createBill(1L, "Sequenced Bill", new BigDecimal("500.00"), 5);
        savedBill.setInstallmentAmount(new BigDecimal("100.00"));
        Long creditCardId = 1L;
        CreditCard creditCard = TestDataBuilder.createCreditCard();
        AvailableLimitResult limitResult = TestDataBuilder.createAvailableLimitResult();

        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(getAvailableLimit.execute(creditCardId)).thenReturn(limitResult);
        when(createBill.addBill(inputBill)).thenReturn(savedBill);

        for (int i = 1; i <= 5; i++) {
            when(installmentDateCalculator.calculate(any(), anyInt(), anyInt(), eq(i)))
                    .thenReturn(TestDataBuilder.createInstallmentDateInfo(i, LocalDate.of(2025, 1, 17), LocalDate.of(2025, 1, 1)));
        }

        when(generateOrGetInvoiceForMonth.execute(anyLong(), any())).thenReturn(TestDataBuilder.createInvoice());
        when(invoiceRepository.saveAll(anyList())).thenReturn(List.of());
        when(installmentRepository.saveAll(anyList())).thenReturn(List.of());

        // When
        useCase.execute(inputBill, creditCardId);

        // Then
        verify(installmentRepository).saveAll(installmentListCaptor.capture());
        List<Installment> savedInstallments = installmentListCaptor.getValue();

        assertThat(savedInstallments).hasSize(5);
        assertThat(savedInstallments).extracting(Installment::getInstallmentNumber)
                .containsExactly(1, 2, 3, 4, 5);
        assertThat(savedInstallments).allMatch(i -> i.getBillId().equals(1L));
        assertThat(savedInstallments).allMatch(i -> i.getCreditCardId().equals(creditCardId));
        assertThat(savedInstallments).allMatch(i -> i.getAmount().compareTo(new BigDecimal("100.00")) == 0);
    }

    @Test
    @DisplayName("BR-I-005: Should calculate invoice total amount correctly")
    void shouldCalculateInvoiceTotalAmountCorrectly() {
        // Given: Bill with 3 installments of 100.00 each, adding to existing invoice with 200.00
        Bill inputBill = TestDataBuilder.createBill(null, "Invoice Total Test", new BigDecimal("300.00"), 3);
        Bill savedBill = TestDataBuilder.createBill(1L, "Invoice Total Test", new BigDecimal("300.00"), 3);
        savedBill.setInstallmentAmount(new BigDecimal("100.00"));
        Long creditCardId = 1L;
        CreditCard creditCard = TestDataBuilder.createCreditCard();
        AvailableLimitResult limitResult = TestDataBuilder.createAvailableLimitResult();

        // Existing invoice with 200.00
        Invoice existingInvoice = TestDataBuilder.createInvoice(1L, creditCardId, LocalDate.of(2025, 1, 1), new BigDecimal("200.00"));

        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(getAvailableLimit.execute(creditCardId)).thenReturn(limitResult);
        when(createBill.addBill(inputBill)).thenReturn(savedBill);
        when(generateOrGetInvoiceForMonth.execute(creditCardId, LocalDate.of(2025, 1, 1))).thenReturn(existingInvoice);

        for (int i = 1; i <= 3; i++) {
            when(installmentDateCalculator.calculate(any(), anyInt(), anyInt(), eq(i)))
                    .thenReturn(TestDataBuilder.createInstallmentDateInfo(i, LocalDate.of(2025, 1, 17), LocalDate.of(2025, 1, 1)));
        }

        when(invoiceRepository.saveAll(anyList())).thenReturn(List.of());
        when(installmentRepository.saveAll(anyList())).thenReturn(List.of());

        // When
        useCase.execute(inputBill, creditCardId);

        // Then: Invoice total should be 200.00 + (3 * 100.00) = 500.00
        verify(invoiceRepository).saveAll(invoiceListCaptor.capture());
        List<Invoice> savedInvoices = invoiceListCaptor.getValue();

        assertThat(savedInvoices).hasSize(1);
        assertThat(savedInvoices.get(0).getTotalAmount()).isEqualByComparingTo("500.00");
    }

    // ========== INTEGRATION & PERFORMANCE TESTS ==========

    @Test
    @DisplayName("Should use invoice cache to avoid duplicate fetches")
    void shouldUseInvoiceCacheToAvoidDuplicateFetches() {
        // Given: 5 installments all in the same month
        Bill inputBill = TestDataBuilder.createBill(null, "Cache Test", new BigDecimal("500.00"), 5);
        Bill savedBill = TestDataBuilder.createBill(1L, "Cache Test", new BigDecimal("500.00"), 5);
        savedBill.setInstallmentAmount(new BigDecimal("100.00"));
        Long creditCardId = 1L;
        CreditCard creditCard = TestDataBuilder.createCreditCard();
        AvailableLimitResult limitResult = TestDataBuilder.createAvailableLimitResult();
        Invoice invoice = TestDataBuilder.createInvoice(1L, creditCardId, LocalDate.of(2025, 1, 1), BigDecimal.ZERO);

        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(getAvailableLimit.execute(creditCardId)).thenReturn(limitResult);
        when(createBill.addBill(inputBill)).thenReturn(savedBill);
        when(generateOrGetInvoiceForMonth.execute(creditCardId, LocalDate.of(2025, 1, 1))).thenReturn(invoice);

        for (int i = 1; i <= 5; i++) {
            when(installmentDateCalculator.calculate(any(), anyInt(), anyInt(), eq(i)))
                    .thenReturn(TestDataBuilder.createInstallmentDateInfo(i, LocalDate.of(2025, 1, 17), LocalDate.of(2025, 1, 1)));
        }

        when(invoiceRepository.saveAll(anyList())).thenReturn(List.of());
        when(installmentRepository.saveAll(anyList())).thenReturn(List.of());

        // When
        useCase.execute(inputBill, creditCardId);

        // Then: Invoice should be fetched only ONCE despite 5 installments
        verify(generateOrGetInvoiceForMonth, times(1)).execute(creditCardId, LocalDate.of(2025, 1, 1));

        // And invoice total should reflect all 5 installments (5 * 100.00 = 500.00)
        verify(invoiceRepository).saveAll(invoiceListCaptor.capture());
        assertThat(invoiceListCaptor.getValue().get(0).getTotalAmount()).isEqualByComparingTo("500.00");
    }

    @Test
    @DisplayName("Should perform batch save for invoices")
    void shouldPerformBatchSaveForInvoices() {
        // Given: Bill with installments across 3 months
        Bill inputBill = TestDataBuilder.createBill(null, "Batch Test", new BigDecimal("300.00"), 3);
        Bill savedBill = TestDataBuilder.createBill(1L, "Batch Test", new BigDecimal("300.00"), 3);
        savedBill.setInstallmentAmount(new BigDecimal("100.00"));
        Long creditCardId = 1L;
        CreditCard creditCard = TestDataBuilder.createCreditCard();
        AvailableLimitResult limitResult = TestDataBuilder.createAvailableLimitResult();

        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(getAvailableLimit.execute(creditCardId)).thenReturn(limitResult);
        when(createBill.addBill(inputBill)).thenReturn(savedBill);

        Invoice invoice1 = TestDataBuilder.createInvoice(1L, creditCardId, LocalDate.of(2025, 1, 1), BigDecimal.ZERO);
        Invoice invoice2 = TestDataBuilder.createInvoice(2L, creditCardId, LocalDate.of(2025, 2, 1), BigDecimal.ZERO);
        Invoice invoice3 = TestDataBuilder.createInvoice(3L, creditCardId, LocalDate.of(2025, 3, 1), BigDecimal.ZERO);

        when(installmentDateCalculator.calculate(any(), anyInt(), anyInt(), eq(1)))
                .thenReturn(TestDataBuilder.createInstallmentDateInfo(1, LocalDate.of(2025, 1, 17), LocalDate.of(2025, 1, 1)));
        when(installmentDateCalculator.calculate(any(), anyInt(), anyInt(), eq(2)))
                .thenReturn(TestDataBuilder.createInstallmentDateInfo(2, LocalDate.of(2025, 2, 17), LocalDate.of(2025, 2, 1)));
        when(installmentDateCalculator.calculate(any(), anyInt(), anyInt(), eq(3)))
                .thenReturn(TestDataBuilder.createInstallmentDateInfo(3, LocalDate.of(2025, 3, 17), LocalDate.of(2025, 3, 1)));

        when(generateOrGetInvoiceForMonth.execute(creditCardId, LocalDate.of(2025, 1, 1))).thenReturn(invoice1);
        when(generateOrGetInvoiceForMonth.execute(creditCardId, LocalDate.of(2025, 2, 1))).thenReturn(invoice2);
        when(generateOrGetInvoiceForMonth.execute(creditCardId, LocalDate.of(2025, 3, 1))).thenReturn(invoice3);

        when(invoiceRepository.saveAll(anyList())).thenReturn(List.of());
        when(installmentRepository.saveAll(anyList())).thenReturn(List.of());

        // When
        useCase.execute(inputBill, creditCardId);

        // Then: saveAll should be called ONCE with all 3 invoices
        verify(invoiceRepository, times(1)).saveAll(invoiceListCaptor.capture());
        List<Invoice> savedInvoices = invoiceListCaptor.getValue();
        assertThat(savedInvoices).hasSize(3);
    }

    @Test
    @DisplayName("Should perform batch save for installments")
    void shouldPerformBatchSaveForInstallments() {
        // Given: Bill with 10 installments
        Bill inputBill = TestDataBuilder.createBill(null, "Installment Batch", new BigDecimal("1000.00"), 10);
        Bill savedBill = TestDataBuilder.createBill(1L, "Installment Batch", new BigDecimal("1000.00"), 10);
        savedBill.setInstallmentAmount(new BigDecimal("100.00"));
        Long creditCardId = 1L;
        CreditCard creditCard = TestDataBuilder.createCreditCard();
        AvailableLimitResult limitResult = TestDataBuilder.createAvailableLimitResult();

        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(getAvailableLimit.execute(creditCardId)).thenReturn(limitResult);
        when(createBill.addBill(inputBill)).thenReturn(savedBill);
        when(generateOrGetInvoiceForMonth.execute(anyLong(), any())).thenReturn(TestDataBuilder.createInvoice());

        for (int i = 1; i <= 10; i++) {
            when(installmentDateCalculator.calculate(any(), anyInt(), anyInt(), eq(i)))
                    .thenReturn(TestDataBuilder.createInstallmentDateInfo(i, LocalDate.of(2025, 1, 17), LocalDate.of(2025, 1, 1)));
        }

        when(invoiceRepository.saveAll(anyList())).thenReturn(List.of());
        when(installmentRepository.saveAll(anyList())).thenReturn(List.of());

        // When
        useCase.execute(inputBill, creditCardId);

        // Then: saveAll should be called ONCE with all 10 installments
        verify(installmentRepository, times(1)).saveAll(installmentListCaptor.capture());
        List<Installment> savedInstallments = installmentListCaptor.getValue();
        assertThat(savedInstallments).hasSize(10);
    }

    @Test
    @DisplayName("Should set correct fields in created installments")
    void shouldSetCorrectFieldsInCreatedInstallments() {
        // Given
        Bill inputBill = TestDataBuilder.createBill(null, "Field Test", new BigDecimal("200.00"), 2);
        Bill savedBill = TestDataBuilder.createBill(99L, "Field Test", new BigDecimal("200.00"), 2);
        savedBill.setInstallmentAmount(new BigDecimal("100.00"));
        Long creditCardId = 42L;
        CreditCard creditCard = TestDataBuilder.createCreditCard(creditCardId, "Test", new BigDecimal("5000.00"), 15, 20);
        AvailableLimitResult limitResult = TestDataBuilder.createAvailableLimitResult();
        Invoice invoice = TestDataBuilder.createInvoice(77L, creditCardId, LocalDate.of(2025, 2, 1), BigDecimal.ZERO);

        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(getAvailableLimit.execute(creditCardId)).thenReturn(limitResult);
        when(createBill.addBill(inputBill)).thenReturn(savedBill);
        when(generateOrGetInvoiceForMonth.execute(creditCardId, LocalDate.of(2025, 2, 1))).thenReturn(invoice);

        when(installmentDateCalculator.calculate(any(), eq(15), eq(20), eq(1)))
                .thenReturn(TestDataBuilder.createInstallmentDateInfo(1, LocalDate.of(2025, 2, 20), LocalDate.of(2025, 2, 1)));
        when(installmentDateCalculator.calculate(any(), eq(15), eq(20), eq(2)))
                .thenReturn(TestDataBuilder.createInstallmentDateInfo(2, LocalDate.of(2025, 2, 20), LocalDate.of(2025, 2, 1)));

        when(invoiceRepository.saveAll(anyList())).thenReturn(List.of());
        when(installmentRepository.saveAll(anyList())).thenReturn(List.of());

        // When
        useCase.execute(inputBill, creditCardId);

        // Then: Verify all installment fields are set correctly
        verify(installmentRepository).saveAll(installmentListCaptor.capture());
        List<Installment> savedInstallments = installmentListCaptor.getValue();

        assertThat(savedInstallments).hasSize(2);

        Installment first = savedInstallments.get(0);
        assertThat(first.getBillId()).isEqualTo(99L);
        assertThat(first.getCreditCardId()).isEqualTo(42L);
        assertThat(first.getInvoiceId()).isEqualTo(77L);
        assertThat(first.getInstallmentNumber()).isEqualTo(1);
        assertThat(first.getAmount()).isEqualByComparingTo("100.00");
        assertThat(first.getDueDate()).isEqualTo(LocalDate.of(2025, 2, 20));
        assertThat(first.getCreatedAt()).isNotNull();

        Installment second = savedInstallments.get(1);
        assertThat(second.getInstallmentNumber()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should propagate exceptions from dependencies")
    void shouldPropagateExceptionsFromDependencies() {
        // Given: InstallmentDateCalculator throws exception
        Bill inputBill = TestDataBuilder.createBill();
        Long creditCardId = 1L;
        CreditCard creditCard = TestDataBuilder.createCreditCard();
        AvailableLimitResult limitResult = TestDataBuilder.createAvailableLimitResult();

        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(getAvailableLimit.execute(creditCardId)).thenReturn(limitResult);
        when(createBill.addBill(inputBill)).thenReturn(TestDataBuilder.createBill());
        when(installmentDateCalculator.calculate(any(), anyInt(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Date calculation failed"));

        // When & Then: Exception should propagate
        assertThatThrownBy(() -> useCase.execute(inputBill, creditCardId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Date calculation failed");
    }

    @Test
    @DisplayName("Should call InstallmentDateCalculator with correct parameters")
    void shouldCallInstallmentDateCalculatorWithCorrectParameters() {
        // Given
        LocalDateTime executionDate = LocalDateTime.of(2025, 6, 15, 14, 30);
        Bill inputBill = TestDataBuilder.createBill(null, "Date Test", new BigDecimal("100.00"), 1);
        inputBill.setExecutionDate(executionDate);
        Bill savedBill = TestDataBuilder.createBill(1L, "Date Test", new BigDecimal("100.00"), 1);
        savedBill.setExecutionDate(executionDate);

        Long creditCardId = 1L;
        CreditCard creditCard = TestDataBuilder.createCreditCard(creditCardId, "Card", new BigDecimal("5000.00"), 25, 5);
        AvailableLimitResult limitResult = TestDataBuilder.createAvailableLimitResult();

        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(getAvailableLimit.execute(creditCardId)).thenReturn(limitResult);
        when(createBill.addBill(inputBill)).thenReturn(savedBill);
        when(installmentDateCalculator.calculate(executionDate, 25, 5, 1))
                .thenReturn(TestDataBuilder.createInstallmentDateInfo());
        when(generateOrGetInvoiceForMonth.execute(anyLong(), any())).thenReturn(TestDataBuilder.createInvoice());
        when(invoiceRepository.saveAll(anyList())).thenReturn(List.of());
        when(installmentRepository.saveAll(anyList())).thenReturn(List.of());

        // When
        useCase.execute(inputBill, creditCardId);

        // Then: Verify calculator was called with exact parameters
        verify(installmentDateCalculator).calculate(executionDate, 25, 5, 1);
    }

    @Test
    @DisplayName("Should return the created bill from CreateBill use case")
    void shouldReturnCreatedBillFromCreateBillUseCase() {
        // Given
        Bill inputBill = TestDataBuilder.createBill(null, "Return Test", new BigDecimal("500.00"), 1);
        Bill expectedBill = TestDataBuilder.createBill(123L, "Return Test", new BigDecimal("500.00"), 1);
        Long creditCardId = 1L;
        CreditCard creditCard = TestDataBuilder.createCreditCard();
        AvailableLimitResult limitResult = TestDataBuilder.createAvailableLimitResult();

        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(getAvailableLimit.execute(creditCardId)).thenReturn(limitResult);
        when(createBill.addBill(inputBill)).thenReturn(expectedBill);
        when(installmentDateCalculator.calculate(any(), anyInt(), anyInt(), anyInt()))
                .thenReturn(TestDataBuilder.createInstallmentDateInfo());
        when(generateOrGetInvoiceForMonth.execute(anyLong(), any())).thenReturn(TestDataBuilder.createInvoice());
        when(invoiceRepository.saveAll(anyList())).thenReturn(List.of());
        when(installmentRepository.saveAll(anyList())).thenReturn(List.of());

        // When
        Bill result = useCase.execute(inputBill, creditCardId);

        // Then: Should return the exact bill from CreateBill
        assertThat(result).isSameAs(expectedBill);
        assertThat(result.getId()).isEqualTo(123L);
        assertThat(result.getName()).isEqualTo("Return Test");
    }
}
