package com.truebalance.truebalance.domain.usecase.invoice;

import com.truebalance.truebalance.domain.entity.Invoice;
import com.truebalance.truebalance.domain.port.InvoiceRepositoryPort;
import com.truebalance.truebalance.domain.usecase.UpdateInvoiceTotalAmount;
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
 * Tests for UpdateInvoiceTotalAmount use case.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateInvoiceTotalAmount - Tests")
class UpdateInvoiceTotalAmountTest {

    @Mock
    private InvoiceRepositoryPort invoiceRepository;

    private UpdateInvoiceTotalAmount useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateInvoiceTotalAmount(invoiceRepository);
    }

    @Test
    @DisplayName("Should update total amount when useAbsoluteValue is enabled")
    void shouldUpdateTotalAmountWhenUseAbsoluteValueIsEnabled() {
        // Given
        Long invoiceId = 1L;
        BigDecimal newTotalAmount = new BigDecimal("1500.00");
        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, 1L, LocalDate.of(2025, 1, 1), new BigDecimal("1000.00"));
        invoice.setUseAbsoluteValue(true);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Optional<Invoice> result = useCase.execute(invoiceId, newTotalAmount);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTotalAmount()).isEqualByComparingTo(newTotalAmount);
        verify(invoiceRepository).save(invoice);
    }

    @Test
    @DisplayName("Should throw IllegalStateException when useAbsoluteValue is disabled")
    void shouldThrowExceptionWhenUseAbsoluteValueIsDisabled() {
        // Given
        Long invoiceId = 1L;
        BigDecimal newTotalAmount = new BigDecimal("1500.00");
        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, 1L, LocalDate.of(2025, 1, 1), new BigDecimal("1000.00"));
        invoice.setUseAbsoluteValue(false);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));

        // When / Then
        assertThatThrownBy(() -> useCase.execute(invoiceId, newTotalAmount))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot manually update total amount when useAbsoluteValue is disabled");

        verify(invoiceRepository, never()).save(any(Invoice.class));
    }

    @Test
    @DisplayName("Should return empty when invoice not found")
    void shouldReturnEmptyWhenInvoiceNotFound() {
        // Given
        Long nonExistentId = 999L;
        BigDecimal newTotalAmount = new BigDecimal("1500.00");

        when(invoiceRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        Optional<Invoice> result = useCase.execute(nonExistentId, newTotalAmount);

        // Then
        assertThat(result).isEmpty();
        verify(invoiceRepository, never()).save(any(Invoice.class));
    }

    @Test
    @DisplayName("Should update total amount to zero when useAbsoluteValue is enabled")
    void shouldUpdateTotalAmountToZero() {
        // Given
        Long invoiceId = 1L;
        BigDecimal newTotalAmount = BigDecimal.ZERO;
        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, 1L, LocalDate.of(2025, 1, 1), new BigDecimal("1000.00"));
        invoice.setUseAbsoluteValue(true);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Optional<Invoice> result = useCase.execute(invoiceId, newTotalAmount);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(invoiceRepository).save(invoice);
    }
}
