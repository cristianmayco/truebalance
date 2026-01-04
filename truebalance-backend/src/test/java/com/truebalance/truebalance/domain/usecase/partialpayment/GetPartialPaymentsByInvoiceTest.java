package com.truebalance.truebalance.domain.usecase.partialpayment;

import com.truebalance.truebalance.domain.entity.PartialPayment;
import com.truebalance.truebalance.domain.port.PartialPaymentRepositoryPort;
import com.truebalance.truebalance.domain.usecase.GetPartialPaymentsByInvoice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for GetPartialPaymentsByInvoice use case.
 *
 * This use case retrieves all partial payments for a specific invoice,
 * ordered by payment date (most recent first).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetPartialPaymentsByInvoice - Retrieval Tests")
class GetPartialPaymentsByInvoiceTest {

    @Mock
    private PartialPaymentRepositoryPort partialPaymentRepository;

    private GetPartialPaymentsByInvoice useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetPartialPaymentsByInvoice(partialPaymentRepository);
    }

    @Test
    @DisplayName("Should return empty list when invoice has no partial payments")
    void shouldReturnEmptyListWhenNoPartialPayments() {
        // Given: Invoice with no partial payments
        Long invoiceId = 1L;

        when(partialPaymentRepository.findByInvoiceId(invoiceId)).thenReturn(Collections.emptyList());

        // When
        List<PartialPayment> result = useCase.execute(invoiceId);

        // Then
        assertThat(result).isNotNull().isEmpty();
        verify(partialPaymentRepository).findByInvoiceId(invoiceId);
    }

    @Test
    @DisplayName("Should return list of partial payments for invoice")
    void shouldReturnListOfPartialPayments() {
        // Given: Invoice with multiple partial payments
        Long invoiceId = 1L;

        PartialPayment payment1 = createPartialPayment(1L, invoiceId, new BigDecimal("100.00"), 
                LocalDateTime.of(2025, 1, 10, 10, 0));
        PartialPayment payment2 = createPartialPayment(2L, invoiceId, new BigDecimal("150.00"), 
                LocalDateTime.of(2025, 1, 15, 14, 30));
        PartialPayment payment3 = createPartialPayment(3L, invoiceId, new BigDecimal("50.00"), 
                LocalDateTime.of(2025, 1, 20, 9, 15));

        List<PartialPayment> expectedPayments = Arrays.asList(payment1, payment2, payment3);

        when(partialPaymentRepository.findByInvoiceId(invoiceId)).thenReturn(expectedPayments);

        // When
        List<PartialPayment> result = useCase.execute(invoiceId);

        // Then
        assertThat(result).isNotNull().hasSize(3);
        assertThat(result).containsExactlyElementsOf(expectedPayments);
        verify(partialPaymentRepository).findByInvoiceId(invoiceId);
    }

    @Test
    @DisplayName("Should return single partial payment")
    void shouldReturnSinglePartialPayment() {
        // Given: Invoice with one partial payment
        Long invoiceId = 1L;

        PartialPayment payment = createPartialPayment(1L, invoiceId, new BigDecimal("200.00"), 
                LocalDateTime.of(2025, 1, 10, 10, 0));

        when(partialPaymentRepository.findByInvoiceId(invoiceId)).thenReturn(List.of(payment));

        // When
        List<PartialPayment> result = useCase.execute(invoiceId);

        // Then
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.get(0)).isEqualTo(payment);
        assertThat(result.get(0).getAmount()).isEqualByComparingTo("200.00");
        verify(partialPaymentRepository).findByInvoiceId(invoiceId);
    }

    @Test
    @DisplayName("Should handle different invoice IDs correctly")
    void shouldHandleDifferentInvoiceIds() {
        // Given: Two different invoices with different payments
        Long invoiceId1 = 1L;
        Long invoiceId2 = 2L;

        PartialPayment payment1 = createPartialPayment(1L, invoiceId1, new BigDecimal("100.00"), 
                LocalDateTime.now());
        PartialPayment payment2 = createPartialPayment(2L, invoiceId2, new BigDecimal("200.00"), 
                LocalDateTime.now());

        when(partialPaymentRepository.findByInvoiceId(invoiceId1)).thenReturn(List.of(payment1));
        when(partialPaymentRepository.findByInvoiceId(invoiceId2)).thenReturn(List.of(payment2));

        // When
        List<PartialPayment> result1 = useCase.execute(invoiceId1);
        List<PartialPayment> result2 = useCase.execute(invoiceId2);

        // Then
        assertThat(result1).hasSize(1);
        assertThat(result1.get(0).getInvoiceId()).isEqualTo(invoiceId1);
        
        assertThat(result2).hasSize(1);
        assertThat(result2.get(0).getInvoiceId()).isEqualTo(invoiceId2);

        verify(partialPaymentRepository).findByInvoiceId(invoiceId1);
        verify(partialPaymentRepository).findByInvoiceId(invoiceId2);
    }

    @Test
    @DisplayName("Should return payments ordered by date (repository responsibility)")
    void shouldReturnPaymentsOrderedByDate() {
        // Given: Payments with different dates (repository should order them)
        Long invoiceId = 1L;

        PartialPayment payment1 = createPartialPayment(1L, invoiceId, new BigDecimal("100.00"), 
                LocalDateTime.of(2025, 1, 10, 10, 0));
        PartialPayment payment2 = createPartialPayment(2L, invoiceId, new BigDecimal("150.00"), 
                LocalDateTime.of(2025, 1, 15, 14, 30));
        PartialPayment payment3 = createPartialPayment(3L, invoiceId, new BigDecimal("50.00"), 
                LocalDateTime.of(2025, 1, 20, 9, 15));

        // Repository returns ordered list (most recent first)
        List<PartialPayment> orderedPayments = Arrays.asList(payment3, payment2, payment1);

        when(partialPaymentRepository.findByInvoiceId(invoiceId)).thenReturn(orderedPayments);

        // When
        List<PartialPayment> result = useCase.execute(invoiceId);

        // Then: Should preserve repository ordering
        assertThat(result).hasSize(3);
        assertThat(result.get(0)).isEqualTo(payment3); // Most recent
        assertThat(result.get(1)).isEqualTo(payment2);
        assertThat(result.get(2)).isEqualTo(payment1); // Oldest
    }

    private PartialPayment createPartialPayment(Long id, Long invoiceId, BigDecimal amount, LocalDateTime paymentDate) {
        PartialPayment payment = new PartialPayment();
        payment.setId(id);
        payment.setInvoiceId(invoiceId);
        payment.setAmount(amount);
        payment.setPaymentDate(paymentDate);
        payment.setDescription("Test payment " + id);
        return payment;
    }
}
