package com.truebalance.truebalance.domain.usecase.invoice;

import com.truebalance.truebalance.domain.entity.Invoice;
import com.truebalance.truebalance.domain.port.InvoiceRepositoryPort;
import com.truebalance.truebalance.domain.usecase.GetInvoicesByCreditCard;
import com.truebalance.truebalance.util.TestDataBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for GetInvoicesByCreditCard use case.
 *
 * Tests retrieval of all invoices for a specific credit card.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetInvoicesByCreditCard Use Case Tests")
class GetInvoicesByCreditCardTest {

    @Mock
    private InvoiceRepositoryPort invoiceRepository;

    @InjectMocks
    private GetInvoicesByCreditCard useCase;

    // ==================== Happy Path Tests ====================

    @Test
    @DisplayName("Should return all invoices for a credit card")
    void shouldReturnAllInvoicesForCreditCard() {
        // Given: Credit card with multiple invoices
        Long creditCardId = 1L;
        Invoice invoice1 = TestDataBuilder.createInvoice(1L, creditCardId, LocalDate.of(2025, 1, 1), new BigDecimal("500.00"));
        Invoice invoice2 = TestDataBuilder.createInvoice(2L, creditCardId, LocalDate.of(2025, 2, 1), new BigDecimal("750.00"));
        Invoice invoice3 = TestDataBuilder.createInvoice(3L, creditCardId, LocalDate.of(2025, 3, 1), new BigDecimal("1000.00"));

        List<Invoice> invoices = List.of(invoice1, invoice2, invoice3);

        when(invoiceRepository.findByCreditCardId(creditCardId)).thenReturn(invoices);

        // When
        List<Invoice> result = useCase.execute(creditCardId);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly(invoice1, invoice2, invoice3);
    }

    @Test
    @DisplayName("Should return empty list when credit card has no invoices")
    void shouldReturnEmptyListWhenCreditCardHasNoInvoices() {
        // Given: Credit card with no invoices
        Long creditCardId = 999L;

        when(invoiceRepository.findByCreditCardId(creditCardId)).thenReturn(Collections.emptyList());

        // When
        List<Invoice> result = useCase.execute(creditCardId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return single invoice when credit card has only one")
    void shouldReturnSingleInvoiceWhenCreditCardHasOnlyOne() {
        // Given: Credit card with single invoice
        Long creditCardId = 1L;
        Invoice invoice = TestDataBuilder.createInvoice(1L, creditCardId, LocalDate.of(2025, 1, 1), new BigDecimal("500.00"));

        when(invoiceRepository.findByCreditCardId(creditCardId)).thenReturn(List.of(invoice));

        // When
        List<Invoice> result = useCase.execute(creditCardId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getCreditCardId()).isEqualTo(creditCardId);
    }

    // ==================== State Tests ====================

    @Test
    @DisplayName("Should return both open and closed invoices")
    void shouldReturnBothOpenAndClosedInvoices() {
        // Given: Mix of open and closed invoices
        Long creditCardId = 1L;
        Invoice openInvoice = TestDataBuilder.createInvoice(1L, creditCardId, LocalDate.of(2025, 3, 1), new BigDecimal("500.00"));
        openInvoice.setClosed(false);

        Invoice closedInvoice = TestDataBuilder.createInvoice(2L, creditCardId, LocalDate.of(2025, 2, 1), new BigDecimal("750.00"));
        closedInvoice.setClosed(true);

        when(invoiceRepository.findByCreditCardId(creditCardId)).thenReturn(List.of(openInvoice, closedInvoice));

        // When
        List<Invoice> result = useCase.execute(creditCardId);

        // Then: Should include both
        assertThat(result).hasSize(2);
        assertThat(result).contains(openInvoice, closedInvoice);
    }

    @Test
    @DisplayName("Should return both paid and unpaid invoices")
    void shouldReturnBothPaidAndUnpaidInvoices() {
        // Given: Mix of paid and unpaid invoices
        Long creditCardId = 1L;
        Invoice paidInvoice = TestDataBuilder.createInvoice(1L, creditCardId, LocalDate.of(2025, 1, 1), new BigDecimal("500.00"));
        paidInvoice.setPaid(true);

        Invoice unpaidInvoice = TestDataBuilder.createInvoice(2L, creditCardId, LocalDate.of(2025, 2, 1), new BigDecimal("750.00"));
        unpaidInvoice.setPaid(false);

        when(invoiceRepository.findByCreditCardId(creditCardId)).thenReturn(List.of(paidInvoice, unpaidInvoice));

        // When
        List<Invoice> result = useCase.execute(creditCardId);

        // Then: Should include both
        assertThat(result).hasSize(2);
        assertThat(result).contains(paidInvoice, unpaidInvoice);
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should return invoices from different years")
    void shouldReturnInvoicesFromDifferentYears() {
        // Given: Invoices across multiple years
        Long creditCardId = 1L;
        Invoice invoice2024 = TestDataBuilder.createInvoice(1L, creditCardId, LocalDate.of(2024, 12, 1), new BigDecimal("500.00"));
        Invoice invoice2025 = TestDataBuilder.createInvoice(2L, creditCardId, LocalDate.of(2025, 1, 1), new BigDecimal("750.00"));
        Invoice invoice2026 = TestDataBuilder.createInvoice(3L, creditCardId, LocalDate.of(2026, 1, 1), new BigDecimal("1000.00"));

        when(invoiceRepository.findByCreditCardId(creditCardId)).thenReturn(List.of(invoice2024, invoice2025, invoice2026));

        // When
        List<Invoice> result = useCase.execute(creditCardId);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result.stream().map(i -> i.getReferenceMonth().getYear()))
                .containsExactly(2024, 2025, 2026);
    }

    @Test
    @DisplayName("Should handle large number of invoices")
    void shouldHandleLargeNumberOfInvoices() {
        // Given: Many invoices (simulating years of history)
        Long creditCardId = 1L;
        List<Invoice> manyInvoices = List.of(
                TestDataBuilder.createInvoice(1L, creditCardId, LocalDate.of(2020, 1, 1), BigDecimal.ZERO),
                TestDataBuilder.createInvoice(2L, creditCardId, LocalDate.of(2020, 2, 1), BigDecimal.ZERO),
                TestDataBuilder.createInvoice(3L, creditCardId, LocalDate.of(2020, 3, 1), BigDecimal.ZERO),
                TestDataBuilder.createInvoice(4L, creditCardId, LocalDate.of(2020, 4, 1), BigDecimal.ZERO),
                TestDataBuilder.createInvoice(5L, creditCardId, LocalDate.of(2020, 5, 1), BigDecimal.ZERO),
                TestDataBuilder.createInvoice(6L, creditCardId, LocalDate.of(2020, 6, 1), BigDecimal.ZERO),
                TestDataBuilder.createInvoice(7L, creditCardId, LocalDate.of(2020, 7, 1), BigDecimal.ZERO),
                TestDataBuilder.createInvoice(8L, creditCardId, LocalDate.of(2020, 8, 1), BigDecimal.ZERO),
                TestDataBuilder.createInvoice(9L, creditCardId, LocalDate.of(2020, 9, 1), BigDecimal.ZERO),
                TestDataBuilder.createInvoice(10L, creditCardId, LocalDate.of(2020, 10, 1), BigDecimal.ZERO)
        );

        when(invoiceRepository.findByCreditCardId(creditCardId)).thenReturn(manyInvoices);

        // When
        List<Invoice> result = useCase.execute(creditCardId);

        // Then
        assertThat(result).hasSize(10);
    }

    @Test
    @DisplayName("Should return invoices with various amounts including zero")
    void shouldReturnInvoicesWithVariousAmountsIncludingZero() {
        // Given: Invoices with different amounts
        Long creditCardId = 1L;
        Invoice zeroInvoice = TestDataBuilder.createInvoice(1L, creditCardId, LocalDate.of(2025, 1, 1), BigDecimal.ZERO);
        Invoice smallInvoice = TestDataBuilder.createInvoice(2L, creditCardId, LocalDate.of(2025, 2, 1), new BigDecimal("0.01"));
        Invoice largeInvoice = TestDataBuilder.createInvoice(3L, creditCardId, LocalDate.of(2025, 3, 1), new BigDecimal("999999.99"));

        when(invoiceRepository.findByCreditCardId(creditCardId)).thenReturn(List.of(zeroInvoice, smallInvoice, largeInvoice));

        // When
        List<Invoice> result = useCase.execute(creditCardId);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(Invoice::getTotalAmount)
                .containsExactly(BigDecimal.ZERO, new BigDecimal("0.01"), new BigDecimal("999999.99"));
    }

    @Test
    @DisplayName("Should only return invoices for the specified credit card")
    void shouldOnlyReturnInvoicesForSpecifiedCreditCard() {
        // Given: Invoices for specific card
        Long creditCardId = 1L;
        Invoice invoice1 = TestDataBuilder.createInvoice(1L, creditCardId, LocalDate.of(2025, 1, 1), new BigDecimal("500.00"));
        Invoice invoice2 = TestDataBuilder.createInvoice(2L, creditCardId, LocalDate.of(2025, 2, 1), new BigDecimal("750.00"));

        when(invoiceRepository.findByCreditCardId(creditCardId)).thenReturn(List.of(invoice1, invoice2));

        // When
        List<Invoice> result = useCase.execute(creditCardId);

        // Then: All invoices should belong to the same card
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(invoice -> invoice.getCreditCardId().equals(creditCardId));
    }
}
