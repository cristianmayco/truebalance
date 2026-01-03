package com.truebalance.truebalance.domain.usecase.invoice;

import com.truebalance.truebalance.domain.entity.Invoice;
import com.truebalance.truebalance.domain.port.InvoiceRepositoryPort;
import com.truebalance.truebalance.domain.usecase.GetInvoiceById;
import com.truebalance.truebalance.util.TestDataBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Tests for GetInvoiceById use case.
 * Simple CRUD operation that retrieves an invoice by its ID.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetInvoiceById - Use Case Tests")
class GetInvoiceByIdTest {

    @Mock
    private InvoiceRepositoryPort invoiceRepository;

    @InjectMocks
    private GetInvoiceById getInvoiceById;

    @Test
    @DisplayName("Should return invoice when exists")
    void shouldReturnInvoiceWhenExists() {
        // Given: Invoice exists in repository
        Long invoiceId = 1L;
        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, 1L, LocalDate.of(2025, 1, 1), new BigDecimal("500.00"));

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));

        // When
        Optional<Invoice> result = getInvoiceById.execute(invoiceId);

        // Then
        assertThat(result)
                .isPresent()
                .hasValueSatisfying(inv -> {
                    assertThat(inv.getId()).isEqualTo(invoiceId);
                    assertThat(inv.getCreditCardId()).isEqualTo(1L);
                    assertThat(inv.getReferenceMonth()).isEqualTo(LocalDate.of(2025, 1, 1));
                    assertThat(inv.getTotalAmount()).isEqualByComparingTo("500.00");
                });
    }

    @Test
    @DisplayName("Should return empty optional when not found")
    void shouldReturnEmptyOptionalWhenNotFound() {
        // Given: Invoice does not exist
        Long nonExistentId = 999L;

        when(invoiceRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        Optional<Invoice> result = getInvoiceById.execute(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle null ID")
    void shouldHandleNullId() {
        // Given: Null ID
        when(invoiceRepository.findById(null)).thenReturn(Optional.empty());

        // When
        Optional<Invoice> result = getInvoiceById.execute(null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should call repository with correct ID")
    void shouldCallRepositoryWithCorrectId() {
        // Given
        Long invoiceId = 42L;
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.empty());

        // When
        getInvoiceById.execute(invoiceId);

        // Then
        verify(invoiceRepository, times(1)).findById(invoiceId);
        verify(invoiceRepository, times(1)).findById(any(Long.class));
    }

    @Test
    @DisplayName("Should map invoice correctly")
    void shouldMapInvoiceCorrectly() {
        // Given: Invoice with all fields populated
        Long invoiceId = 123L;
        Invoice invoice = TestDataBuilder.createInvoice();
        invoice.setId(invoiceId);
        invoice.setCreditCardId(5L);
        invoice.setReferenceMonth(LocalDate.of(2025, 3, 1));
        invoice.setTotalAmount(new BigDecimal("1234.56"));
        invoice.setPreviousBalance(new BigDecimal("100.00"));
        invoice.setClosed(true);
        invoice.setPaid(false);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));

        // When
        Optional<Invoice> result = getInvoiceById.execute(invoiceId);

        // Then: All fields should be present
        assertThat(result).isPresent();
        Invoice returnedInvoice = result.get();
        assertThat(returnedInvoice.getId()).isEqualTo(invoiceId);
        assertThat(returnedInvoice.getCreditCardId()).isEqualTo(5L);
        assertThat(returnedInvoice.getReferenceMonth()).isEqualTo(LocalDate.of(2025, 3, 1));
        assertThat(returnedInvoice.getTotalAmount()).isEqualByComparingTo("1234.56");
        assertThat(returnedInvoice.getPreviousBalance()).isEqualByComparingTo("100.00");
        assertThat(returnedInvoice.isClosed()).isTrue();
        assertThat(returnedInvoice.isPaid()).isFalse();
    }

    @Test
    @DisplayName("Should handle repository exception")
    void shouldHandleRepositoryException() {
        // Given: Repository throws exception
        Long invoiceId = 1L;
        when(invoiceRepository.findById(invoiceId)).thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThatThrownBy(() -> getInvoiceById.execute(invoiceId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database connection failed");
    }
}
