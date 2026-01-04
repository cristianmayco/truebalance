package com.truebalance.truebalance.domain.usecase.partialpayment;

import com.truebalance.truebalance.domain.entity.Invoice;
import com.truebalance.truebalance.domain.entity.PartialPayment;
import com.truebalance.truebalance.domain.exception.InvoiceClosedException;
import com.truebalance.truebalance.domain.exception.PartialPaymentNotFoundException;
import com.truebalance.truebalance.domain.port.InvoiceRepositoryPort;
import com.truebalance.truebalance.domain.port.PartialPaymentRepositoryPort;
import com.truebalance.truebalance.domain.usecase.DeletePartialPayment;
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
 * Tests for DeletePartialPayment use case.
 *
 * Business Rules Tested:
 * - BR-PP-003: Partial payments can only be deleted if invoice is open
 * - BR-PP-004: Partial payments are immutable (cannot be edited, only deleted)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DeletePartialPayment - Business Rule Tests")
class DeletePartialPaymentTest {

    @Mock
    private PartialPaymentRepositoryPort partialPaymentRepository;

    @Mock
    private InvoiceRepositoryPort invoiceRepository;

    private DeletePartialPayment useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeletePartialPayment(partialPaymentRepository, invoiceRepository);
    }

    @Test
    @DisplayName("Should delete partial payment when invoice is open")
    void shouldDeletePartialPaymentWhenInvoiceIsOpen() {
        // Given: Partial payment exists and invoice is open
        Long partialPaymentId = 1L;
        Long invoiceId = 10L;
        
        PartialPayment partialPayment = new PartialPayment();
        partialPayment.setId(partialPaymentId);
        partialPayment.setInvoiceId(invoiceId);
        partialPayment.setAmount(new BigDecimal("100.00"));

        Invoice openInvoice = TestDataBuilder.createInvoice(invoiceId, 1L, LocalDate.of(2025, 1, 1), new BigDecimal("500.00"));
        openInvoice.setClosed(false);

        when(partialPaymentRepository.findById(partialPaymentId)).thenReturn(Optional.of(partialPayment));
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(openInvoice));

        // When
        boolean result = useCase.execute(partialPaymentId);

        // Then: Should delete successfully
        assertThat(result).isTrue();
        verify(partialPaymentRepository).deleteById(partialPaymentId);
    }

    @Test
    @DisplayName("Should throw exception when partial payment not found")
    void shouldThrowExceptionWhenPartialPaymentNotFound() {
        // Given: Partial payment does not exist
        Long nonExistentId = 999L;

        when(partialPaymentRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> useCase.execute(nonExistentId))
                .isInstanceOf(PartialPaymentNotFoundException.class)
                .hasMessageContaining("999");

        verify(partialPaymentRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should throw exception when invoice is closed (BR-PP-003)")
    void shouldThrowExceptionWhenInvoiceIsClosed() {
        // Given: Partial payment exists but invoice is closed
        Long partialPaymentId = 1L;
        Long invoiceId = 10L;
        
        PartialPayment partialPayment = new PartialPayment();
        partialPayment.setId(partialPaymentId);
        partialPayment.setInvoiceId(invoiceId);
        partialPayment.setAmount(new BigDecimal("100.00"));

        Invoice closedInvoice = TestDataBuilder.createInvoice(invoiceId, 1L, LocalDate.of(2025, 1, 1), new BigDecimal("500.00"));
        closedInvoice.setClosed(true);

        when(partialPaymentRepository.findById(partialPaymentId)).thenReturn(Optional.of(partialPayment));
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(closedInvoice));

        // When & Then
        assertThatThrownBy(() -> useCase.execute(partialPaymentId))
                .isInstanceOf(InvoiceClosedException.class)
                .hasMessageContaining("10");

        verify(partialPaymentRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should throw exception when invoice not found")
    void shouldThrowExceptionWhenInvoiceNotFound() {
        // Given: Partial payment exists but invoice does not
        Long partialPaymentId = 1L;
        Long invoiceId = 10L;
        
        PartialPayment partialPayment = new PartialPayment();
        partialPayment.setId(partialPaymentId);
        partialPayment.setInvoiceId(invoiceId);

        when(partialPaymentRepository.findById(partialPaymentId)).thenReturn(Optional.of(partialPayment));
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> useCase.execute(partialPaymentId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invoice not found");

        verify(partialPaymentRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("BR-PP-003: Should prevent deletion from closed invoice")
    void shouldPreventDeletionFromClosedInvoice() {
        // Given: Closed invoice
        Long partialPaymentId = 1L;
        Long invoiceId = 10L;
        
        PartialPayment partialPayment = new PartialPayment();
        partialPayment.setId(partialPaymentId);
        partialPayment.setInvoiceId(invoiceId);

        Invoice closedInvoice = TestDataBuilder.createInvoice(invoiceId, 1L, LocalDate.of(2025, 1, 1), new BigDecimal("500.00"));
        closedInvoice.setClosed(true);

        when(partialPaymentRepository.findById(partialPaymentId)).thenReturn(Optional.of(partialPayment));
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(closedInvoice));

        // When & Then
        assertThatThrownBy(() -> useCase.execute(partialPaymentId))
                .isInstanceOf(InvoiceClosedException.class);

        verify(partialPaymentRepository, never()).deleteById(anyLong());
    }
}
