package com.truebalance.truebalance.domain.usecase.invoice;

import com.truebalance.truebalance.domain.entity.Invoice;
import com.truebalance.truebalance.domain.port.InvoiceRepositoryPort;
import com.truebalance.truebalance.domain.usecase.UpdateInvoiceUseAbsoluteValue;
import com.truebalance.truebalance.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for UpdateInvoiceUseAbsoluteValue use case.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateInvoiceUseAbsoluteValue - Tests")
class UpdateInvoiceUseAbsoluteValueTest {

    @Mock
    private InvoiceRepositoryPort invoiceRepository;

    private UpdateInvoiceUseAbsoluteValue useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateInvoiceUseAbsoluteValue(invoiceRepository);
    }

    @Test
    @DisplayName("Should update useAbsoluteValue flag to true")
    void shouldUpdateUseAbsoluteValueToTrue() {
        // Given
        Long invoiceId = 1L;
        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, 1L, LocalDate.of(2025, 1, 1), new BigDecimal("1000.00"));
        invoice.setUseAbsoluteValue(false);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Optional<Invoice> result = useCase.execute(invoiceId, true);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().isUseAbsoluteValue()).isTrue();
        verify(invoiceRepository).save(invoice);
    }

    @Test
    @DisplayName("Should update useAbsoluteValue flag to false")
    void shouldUpdateUseAbsoluteValueToFalse() {
        // Given
        Long invoiceId = 1L;
        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, 1L, LocalDate.of(2025, 1, 1), new BigDecimal("1000.00"));
        invoice.setUseAbsoluteValue(true);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Optional<Invoice> result = useCase.execute(invoiceId, false);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().isUseAbsoluteValue()).isFalse();
        verify(invoiceRepository).save(invoice);
    }

    @Test
    @DisplayName("Should return empty when invoice not found")
    void shouldReturnEmptyWhenInvoiceNotFound() {
        // Given
        Long nonExistentId = 999L;

        when(invoiceRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        Optional<Invoice> result = useCase.execute(nonExistentId, true);

        // Then
        assertThat(result).isEmpty();
        verify(invoiceRepository, never()).save(any(Invoice.class));
    }
}
