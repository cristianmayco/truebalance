package com.truebalance.truebalance.domain.usecase.creditcard;

import com.truebalance.truebalance.domain.entity.CreditCard;
import com.truebalance.truebalance.domain.entity.Invoice;
import com.truebalance.truebalance.domain.exception.CreditCardNotFoundException;
import com.truebalance.truebalance.domain.port.CreditCardRepositoryPort;
import com.truebalance.truebalance.domain.port.InstallmentRepositoryPort;
import com.truebalance.truebalance.domain.port.InvoiceRepositoryPort;
import com.truebalance.truebalance.domain.port.PartialPaymentRepositoryPort;
import com.truebalance.truebalance.domain.usecase.AvailableLimitResult;
import com.truebalance.truebalance.domain.usecase.GetAvailableLimit;
import com.truebalance.truebalance.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for GetAvailableLimit use case.
 *
 * This is a CRITICAL financial calculation that determines whether
 * a bill can be created on a credit card.
 *
 * Business Rule Tested:
 * BR-CC-008: availableLimit = creditLimit - usedLimit + partialPaymentsTotal
 *
 * Where:
 * - creditLimit: Fixed total limit of the card
 * - usedLimit: Sum of all installments in OPEN and UNPAID invoices
 * - partialPaymentsTotal: Sum of all partial payments in OPEN and UNPAID invoices
 *
 * Only OPEN and UNPAID invoices (closed = false AND paid = false) are considered.
 * Paid invoices do not consume credit limit.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetAvailableLimit - Critical Financial Calculation Tests")
class GetAvailableLimitTest {

    @Mock
    private CreditCardRepositoryPort creditCardRepository;

    @Mock
    private InvoiceRepositoryPort invoiceRepository;

    @Mock
    private InstallmentRepositoryPort installmentRepository;

    @Mock
    private PartialPaymentRepositoryPort partialPaymentRepository;

    private GetAvailableLimit useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetAvailableLimit(
                creditCardRepository,
                invoiceRepository,
                installmentRepository,
                partialPaymentRepository
        );
    }

    // ========== HAPPY PATH TESTS ==========

    @Test
    @DisplayName("Should return full credit limit when no open invoices exist")
    void shouldReturnFullCreditLimitWhenNoOpenInvoices() {
        // Given: Credit card with 5000.00 limit and no open invoices
        Long creditCardId = 1L;
        CreditCard creditCard = TestDataBuilder.createCreditCard(creditCardId, "Test Card", new BigDecimal("5000.00"), 10, 17);

        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(invoiceRepository.findByCreditCardIdAndClosedAndPaid(creditCardId, false, false)).thenReturn(Collections.emptyList());

        // When
        AvailableLimitResult result = useCase.execute(creditCardId);

        // Then: Full limit should be available
        assertThat(result).isNotNull();
        assertThat(result.getCreditCardId()).isEqualTo(creditCardId);
        assertThat(result.getCreditLimit()).isEqualByComparingTo("5000.00");
        assertThat(result.getUsedLimit()).isEqualByComparingTo("0.00");
        assertThat(result.getPartialPaymentsTotal()).isEqualByComparingTo("0.00");
        assertThat(result.getAvailableLimit()).isEqualByComparingTo("5000.00");

        // Verify installments and payments were NOT queried (optimization)
        verify(installmentRepository, never()).sumAmountByInvoiceIds(anyList());
        verify(partialPaymentRepository, never()).sumAmountByInvoiceIds(anyList());
    }

    @Test
    @DisplayName("Should return full limit when open invoices exist but have no installments")
    void shouldReturnFullLimitWhenOpenInvoicesHaveNoInstallments() {
        // Given: Open invoice exists but no installments or payments
        Long creditCardId = 1L;
        CreditCard creditCard = TestDataBuilder.createCreditCard(creditCardId, "Test Card", new BigDecimal("3000.00"), 10, 17);
        Invoice openInvoice = TestDataBuilder.createInvoice(1L, creditCardId, LocalDate.of(2025, 1, 1), BigDecimal.ZERO);
        openInvoice.setClosed(false);

        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(invoiceRepository.findByCreditCardIdAndClosedAndPaid(creditCardId, false, false)).thenReturn(List.of(openInvoice));
        when(installmentRepository.sumAmountByInvoiceIds(List.of(1L))).thenReturn(BigDecimal.ZERO);
        when(partialPaymentRepository.sumAmountByInvoiceIds(List.of(1L))).thenReturn(BigDecimal.ZERO);

        // When
        AvailableLimitResult result = useCase.execute(creditCardId);

        // Then: Full limit available
        assertThat(result.getAvailableLimit()).isEqualByComparingTo("3000.00");
        assertThat(result.getUsedLimit()).isEqualByComparingTo("0.00");
        assertThat(result.getPartialPaymentsTotal()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Should calculate available limit with installments")
    void shouldCalculateAvailableLimitWithInstallments() {
        // Given: Credit card with 5000.00 limit, 1500.00 in installments
        Long creditCardId = 1L;
        CreditCard creditCard = TestDataBuilder.createCreditCard(creditCardId, "Test Card", new BigDecimal("5000.00"), 10, 17);
        Invoice openInvoice = TestDataBuilder.createInvoice(1L, creditCardId, LocalDate.of(2025, 1, 1), new BigDecimal("1500.00"));
        openInvoice.setClosed(false);

        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(invoiceRepository.findByCreditCardIdAndClosedAndPaid(creditCardId, false, false)).thenReturn(List.of(openInvoice));
        when(installmentRepository.sumAmountByInvoiceIds(List.of(1L))).thenReturn(new BigDecimal("1500.00"));
        when(partialPaymentRepository.sumAmountByInvoiceIds(List.of(1L))).thenReturn(BigDecimal.ZERO);

        // When
        AvailableLimitResult result = useCase.execute(creditCardId);

        // Then: 5000.00 - 1500.00 = 3500.00 available
        assertThat(result.getCreditLimit()).isEqualByComparingTo("5000.00");
        assertThat(result.getUsedLimit()).isEqualByComparingTo("1500.00");
        assertThat(result.getPartialPaymentsTotal()).isEqualByComparingTo("0.00");
        assertThat(result.getAvailableLimit()).isEqualByComparingTo("3500.00");
    }

    @Test
    @DisplayName("Should calculate available limit with partial payments")
    void shouldCalculateAvailableLimitWithPartialPayments() {
        // Given: Credit card with 5000.00 limit, 2000.00 in installments, 500.00 in partial payments
        Long creditCardId = 1L;
        CreditCard creditCard = TestDataBuilder.createCreditCard(creditCardId, "Test Card", new BigDecimal("5000.00"), 10, 17);
        Invoice openInvoice = TestDataBuilder.createInvoice(1L, creditCardId, LocalDate.of(2025, 1, 1), new BigDecimal("2000.00"));
        openInvoice.setClosed(false);

        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(invoiceRepository.findByCreditCardIdAndClosedAndPaid(creditCardId, false, false)).thenReturn(List.of(openInvoice));
        when(installmentRepository.sumAmountByInvoiceIds(List.of(1L))).thenReturn(new BigDecimal("2000.00"));
        when(partialPaymentRepository.sumAmountByInvoiceIds(List.of(1L))).thenReturn(new BigDecimal("500.00"));

        // When
        AvailableLimitResult result = useCase.execute(creditCardId);

        // Then: 5000.00 - 2000.00 + 500.00 = 3500.00 available
        assertThat(result.getCreditLimit()).isEqualByComparingTo("5000.00");
        assertThat(result.getUsedLimit()).isEqualByComparingTo("2000.00");
        assertThat(result.getPartialPaymentsTotal()).isEqualByComparingTo("500.00");
        assertThat(result.getAvailableLimit()).isEqualByComparingTo("3500.00");
    }

    @Test
    @DisplayName("Should handle multiple open invoices")
    void shouldHandleMultipleOpenInvoices() {
        // Given: 3 open invoices with different amounts
        Long creditCardId = 1L;
        CreditCard creditCard = TestDataBuilder.createCreditCard(creditCardId, "Test Card", new BigDecimal("10000.00"), 10, 17);

        Invoice invoice1 = TestDataBuilder.createInvoice(1L, creditCardId, LocalDate.of(2025, 1, 1), new BigDecimal("1000.00"));
        invoice1.setClosed(false);
        Invoice invoice2 = TestDataBuilder.createInvoice(2L, creditCardId, LocalDate.of(2025, 2, 1), new BigDecimal("2000.00"));
        invoice2.setClosed(false);
        Invoice invoice3 = TestDataBuilder.createInvoice(3L, creditCardId, LocalDate.of(2025, 3, 1), new BigDecimal("1500.00"));
        invoice3.setClosed(false);

        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(invoiceRepository.findByCreditCardIdAndClosedAndPaid(creditCardId, false, false))
                .thenReturn(Arrays.asList(invoice1, invoice2, invoice3));
        when(installmentRepository.sumAmountByInvoiceIds(Arrays.asList(1L, 2L, 3L)))
                .thenReturn(new BigDecimal("4500.00")); // Total across all invoices
        when(partialPaymentRepository.sumAmountByInvoiceIds(Arrays.asList(1L, 2L, 3L)))
                .thenReturn(new BigDecimal("1000.00"));

        // When
        AvailableLimitResult result = useCase.execute(creditCardId);

        // Then: 10000.00 - 4500.00 + 1000.00 = 6500.00
        assertThat(result.getAvailableLimit()).isEqualByComparingTo("6500.00");
        assertThat(result.getUsedLimit()).isEqualByComparingTo("4500.00");
        assertThat(result.getPartialPaymentsTotal()).isEqualByComparingTo("1000.00");
    }

    // ========== VALIDATION & EXCEPTION TESTS ==========

    @Test
    @DisplayName("Should throw exception when credit card not found")
    void shouldThrowExceptionWhenCreditCardNotFound() {
        // Given: Credit card does not exist
        Long nonExistentCardId = 999L;

        when(creditCardRepository.findById(nonExistentCardId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> useCase.execute(nonExistentCardId))
                .isInstanceOf(CreditCardNotFoundException.class)
                .hasMessageContaining("999");

        // Verify no further queries were made
        verify(invoiceRepository, never()).findByCreditCardIdAndClosedAndPaid(anyLong(), anyBoolean(), anyBoolean());
    }

    // ========== EDGE CASES ==========

    @Test
    @DisplayName("Should return zero available when limit is fully used")
    void shouldReturnZeroAvailableWhenLimitFullyUsed() {
        // Given: Credit limit exactly equals used limit
        Long creditCardId = 1L;
        CreditCard creditCard = TestDataBuilder.createCreditCard(creditCardId, "Test Card", new BigDecimal("5000.00"), 10, 17);
        Invoice openInvoice = TestDataBuilder.createInvoice(1L, creditCardId, LocalDate.of(2025, 1, 1), new BigDecimal("5000.00"));
        openInvoice.setClosed(false);

        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(invoiceRepository.findByCreditCardIdAndClosedAndPaid(creditCardId, false, false)).thenReturn(List.of(openInvoice));
        when(installmentRepository.sumAmountByInvoiceIds(List.of(1L))).thenReturn(new BigDecimal("5000.00"));
        when(partialPaymentRepository.sumAmountByInvoiceIds(List.of(1L))).thenReturn(BigDecimal.ZERO);

        // When
        AvailableLimitResult result = useCase.execute(creditCardId);

        // Then: 5000.00 - 5000.00 = 0.00 available
        assertThat(result.getAvailableLimit()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Should return negative available when limit is exceeded")
    void shouldReturnNegativeAvailableWhenLimitExceeded() {
        // Given: Used limit exceeds credit limit (edge case that shouldn't happen but can theoretically)
        Long creditCardId = 1L;
        CreditCard creditCard = TestDataBuilder.createCreditCard(creditCardId, "Test Card", new BigDecimal("5000.00"), 10, 17);
        Invoice openInvoice = TestDataBuilder.createInvoice(1L, creditCardId, LocalDate.of(2025, 1, 1), new BigDecimal("6000.00"));
        openInvoice.setClosed(false);

        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(invoiceRepository.findByCreditCardIdAndClosedAndPaid(creditCardId, false, false)).thenReturn(List.of(openInvoice));
        when(installmentRepository.sumAmountByInvoiceIds(List.of(1L))).thenReturn(new BigDecimal("6000.00"));
        when(partialPaymentRepository.sumAmountByInvoiceIds(List.of(1L))).thenReturn(BigDecimal.ZERO);

        // When
        AvailableLimitResult result = useCase.execute(creditCardId);

        // Then: 5000.00 - 6000.00 = -1000.00 available
        assertThat(result.getAvailableLimit()).isEqualByComparingTo("-1000.00");
    }

    @Test
    @DisplayName("Should allow available limit to exceed credit limit with partial payments")
    void shouldAllowAvailableLimitToExceedCreditLimitWithPartialPayments() {
        // Given: Partial payments exceed used limit, creating credit
        Long creditCardId = 1L;
        CreditCard creditCard = TestDataBuilder.createCreditCard(creditCardId, "Test Card", new BigDecimal("5000.00"), 10, 17);
        Invoice openInvoice = TestDataBuilder.createInvoice(1L, creditCardId, LocalDate.of(2025, 1, 1), new BigDecimal("1000.00"));
        openInvoice.setClosed(false);

        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(invoiceRepository.findByCreditCardIdAndClosedAndPaid(creditCardId, false, false)).thenReturn(List.of(openInvoice));
        when(installmentRepository.sumAmountByInvoiceIds(List.of(1L))).thenReturn(new BigDecimal("1000.00"));
        when(partialPaymentRepository.sumAmountByInvoiceIds(List.of(1L))).thenReturn(new BigDecimal("2000.00")); // More than installments

        // When
        AvailableLimitResult result = useCase.execute(creditCardId);

        // Then: 5000.00 - 1000.00 + 2000.00 = 6000.00 available (exceeds credit limit)
        assertThat(result.getAvailableLimit()).isEqualByComparingTo("6000.00");
        assertThat(result.getAvailableLimit()).isGreaterThan(result.getCreditLimit());
    }

    // ========== BUSINESS RULE TESTS ==========

    @Test
    @DisplayName("BR-CC-008: Should apply correct formula for available limit")
    void shouldApplyCorrectFormulaForAvailableLimit() {
        // Given: Specific values to test the exact formula
        Long creditCardId = 1L;
        CreditCard creditCard = TestDataBuilder.createCreditCard(creditCardId, "Test Card", new BigDecimal("8000.00"), 10, 17);
        Invoice openInvoice = TestDataBuilder.createInvoice(1L, creditCardId, LocalDate.of(2025, 1, 1), BigDecimal.ZERO);
        openInvoice.setClosed(false);

        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(invoiceRepository.findByCreditCardIdAndClosedAndPaid(creditCardId, false, false)).thenReturn(List.of(openInvoice));
        when(installmentRepository.sumAmountByInvoiceIds(List.of(1L))).thenReturn(new BigDecimal("3250.75"));
        when(partialPaymentRepository.sumAmountByInvoiceIds(List.of(1L))).thenReturn(new BigDecimal("1100.50"));

        // When
        AvailableLimitResult result = useCase.execute(creditCardId);

        // Then: 8000.00 - 3250.75 + 1100.50 = 5849.75
        assertThat(result.getCreditLimit()).isEqualByComparingTo("8000.00");
        assertThat(result.getUsedLimit()).isEqualByComparingTo("3250.75");
        assertThat(result.getPartialPaymentsTotal()).isEqualByComparingTo("1100.50");
        assertThat(result.getAvailableLimit()).isEqualByComparingTo("5849.75");

        // Verify formula: creditLimit - usedLimit + partialPayments
        BigDecimal expected = result.getCreditLimit()
                .subtract(result.getUsedLimit())
                .add(result.getPartialPaymentsTotal());
        assertThat(result.getAvailableLimit()).isEqualByComparingTo(expected);
    }

    @Test
    @DisplayName("Should only consider OPEN invoices (closed = false)")
    void shouldOnlyConsiderOpenInvoices() {
        // Given: Credit card with both open and closed invoices
        Long creditCardId = 1L;
        CreditCard creditCard = TestDataBuilder.createCreditCard(creditCardId, "Test Card", new BigDecimal("5000.00"), 10, 17);

        Invoice openInvoice = TestDataBuilder.createInvoice(1L, creditCardId, LocalDate.of(2025, 1, 1), new BigDecimal("1000.00"));
        openInvoice.setClosed(false);

        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(invoiceRepository.findByCreditCardIdAndClosedAndPaid(creditCardId, false, false)).thenReturn(List.of(openInvoice));
        when(installmentRepository.sumAmountByInvoiceIds(List.of(1L))).thenReturn(new BigDecimal("1000.00"));
        when(partialPaymentRepository.sumAmountByInvoiceIds(List.of(1L))).thenReturn(BigDecimal.ZERO);

        // When
        useCase.execute(creditCardId);

        // Then: Verify only closed=false and paid=false was queried
        verify(invoiceRepository).findByCreditCardIdAndClosedAndPaid(creditCardId, false, false);
        verify(invoiceRepository, never()).findByCreditCardIdAndClosedAndPaid(creditCardId, true, anyBoolean());
    }

    @Test
    @DisplayName("Should ignore paid invoices - they do not consume credit limit")
    void shouldIgnorePaidInvoices() {
        // Given: Credit card with paid invoice (paid = true, closed = false)
        Long creditCardId = 1L;
        CreditCard creditCard = TestDataBuilder.createCreditCard(creditCardId, "Test Card", new BigDecimal("5000.00"), 10, 17);
        
        Invoice paidInvoice = TestDataBuilder.createInvoice(1L, creditCardId, LocalDate.of(2025, 1, 1), new BigDecimal("2000.00"));
        paidInvoice.setClosed(false);
        paidInvoice.setPaid(true); // Invoice is paid

        // Paid invoice should not be returned by repository query
        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(invoiceRepository.findByCreditCardIdAndClosedAndPaid(creditCardId, false, false))
                .thenReturn(Collections.emptyList()); // No unpaid invoices

        // When
        AvailableLimitResult result = useCase.execute(creditCardId);

        // Then: Full limit available because paid invoice doesn't consume limit
        assertThat(result.getUsedLimit()).isEqualByComparingTo("0.00");
        assertThat(result.getAvailableLimit()).isEqualByComparingTo("5000.00");
        
        // Verify that only unpaid invoices were queried
        verify(invoiceRepository).findByCreditCardIdAndClosedAndPaid(creditCardId, false, false);
        verify(installmentRepository, never()).sumAmountByInvoiceIds(anyList());
    }

    @Test
    @DisplayName("Should ignore closed invoices even if they have installments")
    void shouldIgnoreClosedInvoicesEvenIfTheyHaveInstallments() {
        // Given: Only open invoice returned (closed invoices filtered at repository level)
        Long creditCardId = 1L;
        CreditCard creditCard = TestDataBuilder.createCreditCard(creditCardId, "Test Card", new BigDecimal("5000.00"), 10, 17);
        Invoice openInvoice = TestDataBuilder.createInvoice(1L, creditCardId, LocalDate.of(2025, 1, 1), new BigDecimal("500.00"));
        openInvoice.setClosed(false);

        // Closed invoice exists but not returned by repository
        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(invoiceRepository.findByCreditCardIdAndClosedAndPaid(creditCardId, false, false)).thenReturn(List.of(openInvoice));
        when(installmentRepository.sumAmountByInvoiceIds(List.of(1L))).thenReturn(new BigDecimal("500.00"));
        when(partialPaymentRepository.sumAmountByInvoiceIds(List.of(1L))).thenReturn(BigDecimal.ZERO);

        // When
        AvailableLimitResult result = useCase.execute(creditCardId);

        // Then: Only considers the single open invoice
        assertThat(result.getUsedLimit()).isEqualByComparingTo("500.00");
        assertThat(result.getAvailableLimit()).isEqualByComparingTo("4500.00");
    }

    // ========== INTEGRATION TESTS ==========

    @Test
    @DisplayName("Should use batch queries for installments and payments")
    void shouldUseBatchQueriesForInstallmentsAndPayments() {
        // Given: Multiple open invoices
        Long creditCardId = 1L;
        CreditCard creditCard = TestDataBuilder.createCreditCard(creditCardId, "Test Card", new BigDecimal("5000.00"), 10, 17);

        Invoice invoice1 = TestDataBuilder.createInvoice(1L, creditCardId, LocalDate.of(2025, 1, 1), BigDecimal.ZERO);
        invoice1.setClosed(false);
        Invoice invoice2 = TestDataBuilder.createInvoice(2L, creditCardId, LocalDate.of(2025, 2, 1), BigDecimal.ZERO);
        invoice2.setClosed(false);
        Invoice invoice3 = TestDataBuilder.createInvoice(3L, creditCardId, LocalDate.of(2025, 3, 1), BigDecimal.ZERO);
        invoice3.setClosed(false);

        List<Long> invoiceIds = Arrays.asList(1L, 2L, 3L);

        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(invoiceRepository.findByCreditCardIdAndClosedAndPaid(creditCardId, false, false))
                .thenReturn(Arrays.asList(invoice1, invoice2, invoice3));
        when(installmentRepository.sumAmountByInvoiceIds(invoiceIds)).thenReturn(new BigDecimal("2000.00"));
        when(partialPaymentRepository.sumAmountByInvoiceIds(invoiceIds)).thenReturn(new BigDecimal("500.00"));

        // When
        useCase.execute(creditCardId);

        // Then: Verify batch queries were used (single call with all invoice IDs)
        verify(installmentRepository, times(1)).sumAmountByInvoiceIds(invoiceIds);
        verify(partialPaymentRepository, times(1)).sumAmountByInvoiceIds(invoiceIds);
    }

    @Test
    @DisplayName("Should handle BigDecimal precision correctly")
    void shouldHandleBigDecimalPrecisionCorrectly() {
        // Given: Values with different decimal places
        Long creditCardId = 1L;
        CreditCard creditCard = TestDataBuilder.createCreditCard(creditCardId, "Test Card", new BigDecimal("5000.00"), 10, 17);
        Invoice openInvoice = TestDataBuilder.createInvoice(1L, creditCardId, LocalDate.of(2025, 1, 1), BigDecimal.ZERO);
        openInvoice.setClosed(false);

        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(invoiceRepository.findByCreditCardIdAndClosedAndPaid(creditCardId, false, false)).thenReturn(List.of(openInvoice));
        when(installmentRepository.sumAmountByInvoiceIds(List.of(1L))).thenReturn(new BigDecimal("1234.567"));
        when(partialPaymentRepository.sumAmountByInvoiceIds(List.of(1L))).thenReturn(new BigDecimal("100.123"));

        // When
        AvailableLimitResult result = useCase.execute(creditCardId);

        // Then: 5000.00 - 1234.567 + 100.123 = 3865.556
        assertThat(result.getAvailableLimit()).isEqualByComparingTo("3865.556");
    }

    @Test
    @DisplayName("Should return all fields correctly populated in result")
    void shouldReturnAllFieldsCorrectlyPopulatedInResult() {
        // Given
        Long creditCardId = 42L;
        CreditCard creditCard = TestDataBuilder.createCreditCard(creditCardId, "Test Card", new BigDecimal("7500.00"), 10, 17);
        Invoice openInvoice = TestDataBuilder.createInvoice(1L, creditCardId, LocalDate.of(2025, 1, 1), BigDecimal.ZERO);
        openInvoice.setClosed(false);

        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(invoiceRepository.findByCreditCardIdAndClosedAndPaid(creditCardId, false, false)).thenReturn(List.of(openInvoice));
        when(installmentRepository.sumAmountByInvoiceIds(List.of(1L))).thenReturn(new BigDecimal("2500.00"));
        when(partialPaymentRepository.sumAmountByInvoiceIds(List.of(1L))).thenReturn(new BigDecimal("750.00"));

        // When
        AvailableLimitResult result = useCase.execute(creditCardId);

        // Then: Verify all fields
        assertThat(result).isNotNull();
        assertThat(result.getCreditCardId()).isEqualTo(42L);
        assertThat(result.getCreditLimit()).isNotNull().isEqualByComparingTo("7500.00");
        assertThat(result.getUsedLimit()).isNotNull().isEqualByComparingTo("2500.00");
        assertThat(result.getPartialPaymentsTotal()).isNotNull().isEqualByComparingTo("750.00");
        assertThat(result.getAvailableLimit()).isNotNull().isEqualByComparingTo("5750.00");
    }

    @Test
    @DisplayName("Should handle exact limit boundary")
    void shouldHandleExactLimitBoundary() {
        // Given: Available limit exactly equals credit limit (boundary case)
        Long creditCardId = 1L;
        CreditCard creditCard = TestDataBuilder.createCreditCard(creditCardId, "Test Card", new BigDecimal("5000.00"), 10, 17);
        Invoice openInvoice = TestDataBuilder.createInvoice(1L, creditCardId, LocalDate.of(2025, 1, 1), BigDecimal.ZERO);
        openInvoice.setClosed(false);

        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(invoiceRepository.findByCreditCardIdAndClosedAndPaid(creditCardId, false, false)).thenReturn(List.of(openInvoice));
        when(installmentRepository.sumAmountByInvoiceIds(List.of(1L))).thenReturn(new BigDecimal("1000.00"));
        when(partialPaymentRepository.sumAmountByInvoiceIds(List.of(1L))).thenReturn(new BigDecimal("1000.00"));

        // When
        AvailableLimitResult result = useCase.execute(creditCardId);

        // Then: 5000.00 - 1000.00 + 1000.00 = 5000.00 (equals credit limit)
        assertThat(result.getAvailableLimit()).isEqualByComparingTo("5000.00");
        assertThat(result.getAvailableLimit()).isEqualByComparingTo(result.getCreditLimit());
    }
}
