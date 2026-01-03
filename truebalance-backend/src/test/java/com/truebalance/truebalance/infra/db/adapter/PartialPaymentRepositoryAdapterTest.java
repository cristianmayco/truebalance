package com.truebalance.truebalance.infra.db.adapter;

import com.truebalance.truebalance.domain.entity.PartialPayment;
import com.truebalance.truebalance.infra.db.entity.PartialPaymentEntity;
import com.truebalance.truebalance.infra.db.repository.PartialPaymentRepository;
import com.truebalance.truebalance.util.TestDataBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for PartialPaymentRepositoryAdapter.
 * Tests mapping, aggregation queries (sum, count), and CRUD operations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PartialPaymentRepositoryAdapter Tests")
class PartialPaymentRepositoryAdapterTest {

    @Mock
    private PartialPaymentRepository repository;

    @InjectMocks
    private PartialPaymentRepositoryAdapter adapter;

    @Captor
    private ArgumentCaptor<PartialPaymentEntity> entityCaptor;

    // ==================== save() Tests ====================

    @Test
    @DisplayName("save() - Should map domain to entity and save")
    void shouldMapDomainToEntityAndSave() {
        // Given: Domain PartialPayment
        PartialPayment domainPayment = TestDataBuilder.createPartialPayment(
                null, 1L, new BigDecimal("500.00"), LocalDateTime.of(2025, 1, 15, 14, 30));
        domainPayment.setDescription("Early payment");

        PartialPaymentEntity savedEntity = new PartialPaymentEntity();
        savedEntity.setId(1L);
        savedEntity.setInvoiceId(1L);
        savedEntity.setAmount(new BigDecimal("500.00"));
        savedEntity.setPaymentDate(LocalDateTime.of(2025, 1, 15, 14, 30));
        savedEntity.setDescription("Early payment");
        savedEntity.setCreatedAt(LocalDateTime.now());

        when(repository.save(any(PartialPaymentEntity.class))).thenReturn(savedEntity);

        // When
        PartialPayment result = adapter.save(domainPayment);

        // Then: Should map correctly
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getInvoiceId()).isEqualTo(1L);
        assertThat(result.getAmount()).isEqualByComparingTo("500.00");
        assertThat(result.getPaymentDate()).isEqualTo(LocalDateTime.of(2025, 1, 15, 14, 30));
        assertThat(result.getDescription()).isEqualTo("Early payment");
        assertThat(result.getCreatedAt()).isNotNull();

        // Verify entity mapping
        verify(repository).save(entityCaptor.capture());
        PartialPaymentEntity capturedEntity = entityCaptor.getValue();
        assertThat(capturedEntity.getInvoiceId()).isEqualTo(1L);
        assertThat(capturedEntity.getAmount()).isEqualByComparingTo("500.00");
    }

    @Test
    @DisplayName("save() - Should handle null description")
    void shouldHandleNullDescription() {
        // Given: Payment without description
        PartialPayment domainPayment = TestDataBuilder.createPartialPayment(
                null, 1L, new BigDecimal("300.00"), LocalDateTime.now());
        domainPayment.setDescription(null);

        PartialPaymentEntity savedEntity = new PartialPaymentEntity();
        savedEntity.setId(1L);
        savedEntity.setInvoiceId(1L);
        savedEntity.setAmount(new BigDecimal("300.00"));
        savedEntity.setPaymentDate(domainPayment.getPaymentDate());
        savedEntity.setDescription(null);
        savedEntity.setCreatedAt(LocalDateTime.now());

        when(repository.save(any(PartialPaymentEntity.class))).thenReturn(savedEntity);

        // When
        PartialPayment result = adapter.save(domainPayment);

        // Then: Should handle null
        assertThat(result.getDescription()).isNull();
    }

    // ==================== findById() Tests ====================

    @Test
    @DisplayName("findById() - Should return mapped domain entity when found")
    void shouldReturnMappedDomainEntityWhenFound() {
        // Given: Entity exists
        Long paymentId = 1L;
        PartialPaymentEntity entity = new PartialPaymentEntity();
        entity.setId(paymentId);
        entity.setInvoiceId(1L);
        entity.setAmount(new BigDecimal("750.00"));
        entity.setPaymentDate(LocalDateTime.of(2025, 1, 20, 16, 0));
        entity.setDescription("Credit card payment");
        entity.setCreatedAt(LocalDateTime.of(2025, 1, 20, 16, 0));

        when(repository.findById(paymentId)).thenReturn(Optional.of(entity));

        // When
        Optional<PartialPayment> result = adapter.findById(paymentId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(paymentId);
        assertThat(result.get().getAmount()).isEqualByComparingTo("750.00");
        assertThat(result.get().getDescription()).isEqualTo("Credit card payment");
    }

    @Test
    @DisplayName("findById() - Should return empty Optional when not found")
    void shouldReturnEmptyOptionalWhenNotFound() {
        // Given: Entity does not exist
        Long paymentId = 999L;
        when(repository.findById(paymentId)).thenReturn(Optional.empty());

        // When
        Optional<PartialPayment> result = adapter.findById(paymentId);

        // Then
        assertThat(result).isEmpty();
    }

    // ==================== findByInvoiceId() Tests ====================

    @Test
    @DisplayName("findByInvoiceId() - Should return payments ordered by payment date desc")
    void shouldReturnPaymentsOrderedByPaymentDateDesc() {
        // Given: Invoice with multiple payments
        Long invoiceId = 1L;

        PartialPaymentEntity entity1 = new PartialPaymentEntity();
        entity1.setId(1L);
        entity1.setInvoiceId(invoiceId);
        entity1.setAmount(new BigDecimal("300.00"));
        entity1.setPaymentDate(LocalDateTime.of(2025, 1, 20, 10, 0));
        entity1.setCreatedAt(LocalDateTime.now());

        PartialPaymentEntity entity2 = new PartialPaymentEntity();
        entity2.setId(2L);
        entity2.setInvoiceId(invoiceId);
        entity2.setAmount(new BigDecimal("200.00"));
        entity2.setPaymentDate(LocalDateTime.of(2025, 1, 15, 14, 0));
        entity2.setCreatedAt(LocalDateTime.now());

        when(repository.findByInvoiceIdOrderByPaymentDateDesc(invoiceId))
                .thenReturn(List.of(entity1, entity2));

        // When
        List<PartialPayment> result = adapter.findByInvoiceId(invoiceId);

        // Then: Ordered by payment date desc (newest first)
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPaymentDate()).isEqualTo(LocalDateTime.of(2025, 1, 20, 10, 0));
        assertThat(result.get(1).getPaymentDate()).isEqualTo(LocalDateTime.of(2025, 1, 15, 14, 0));
    }

    @Test
    @DisplayName("findByInvoiceId() - Should return empty list when no payments exist")
    void shouldReturnEmptyListWhenNoPaymentsForInvoice() {
        // Given: No payments for invoice
        Long invoiceId = 999L;
        when(repository.findByInvoiceIdOrderByPaymentDateDesc(invoiceId))
                .thenReturn(List.of());

        // When
        List<PartialPayment> result = adapter.findByInvoiceId(invoiceId);

        // Then
        assertThat(result).isEmpty();
    }

    // ==================== sumByInvoiceId() Tests ====================

    @Test
    @DisplayName("sumByInvoiceId() - Should return sum of partial payments")
    void shouldReturnSumOfPartialPayments() {
        // Given: Invoice with payments totaling 800
        Long invoiceId = 1L;
        BigDecimal sum = new BigDecimal("800.00");

        when(repository.sumByInvoiceId(invoiceId)).thenReturn(sum);

        // When
        BigDecimal result = adapter.sumByInvoiceId(invoiceId);

        // Then
        assertThat(result).isEqualByComparingTo("800.00");
    }

    @Test
    @DisplayName("sumByInvoiceId() - Should return zero when no payments exist")
    void shouldReturnZeroWhenNoPayments() {
        // Given: Invoice with no payments
        Long invoiceId = 1L;
        when(repository.sumByInvoiceId(invoiceId)).thenReturn(BigDecimal.ZERO);

        // When
        BigDecimal result = adapter.sumByInvoiceId(invoiceId);

        // Then
        assertThat(result).isEqualByComparingTo("0.00");
    }

    // ==================== countByInvoiceId() Tests ====================

    @Test
    @DisplayName("countByInvoiceId() - Should return count of partial payments")
    void shouldReturnCountOfPartialPayments() {
        // Given: Invoice with 3 payments
        Long invoiceId = 1L;
        when(repository.countByInvoiceId(invoiceId)).thenReturn(3);

        // When
        int result = adapter.countByInvoiceId(invoiceId);

        // Then
        assertThat(result).isEqualTo(3);
    }

    @Test
    @DisplayName("countByInvoiceId() - Should return zero when no payments exist")
    void shouldReturnZeroCountWhenNoPayments() {
        // Given: Invoice with no payments
        Long invoiceId = 1L;
        when(repository.countByInvoiceId(invoiceId)).thenReturn(0);

        // When
        int result = adapter.countByInvoiceId(invoiceId);

        // Then
        assertThat(result).isEqualTo(0);
    }

    // ==================== sumAmountByInvoiceIds() Tests ====================

    @Test
    @DisplayName("sumAmountByInvoiceIds() - Should return sum across multiple invoices")
    void shouldReturnSumAcrossMultipleInvoices() {
        // Given: Multiple invoices
        List<Long> invoiceIds = List.of(1L, 2L, 3L);
        BigDecimal totalSum = new BigDecimal("3500.00");

        when(repository.sumAmountByInvoiceIds(invoiceIds)).thenReturn(totalSum);

        // When
        BigDecimal result = adapter.sumAmountByInvoiceIds(invoiceIds);

        // Then
        assertThat(result).isEqualByComparingTo("3500.00");
    }

    @Test
    @DisplayName("sumAmountByInvoiceIds() - Should return zero when invoice list is empty")
    void shouldReturnZeroWhenInvoiceListIsEmpty() {
        // Given: Empty list
        List<Long> invoiceIds = List.of();

        // When
        BigDecimal result = adapter.sumAmountByInvoiceIds(invoiceIds);

        // Then: Should return zero without querying
        assertThat(result).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("sumAmountByInvoiceIds() - Should return zero when invoice list is null")
    void shouldReturnZeroWhenInvoiceListIsNull() {
        // Given: Null list
        List<Long> invoiceIds = null;

        // When
        BigDecimal result = adapter.sumAmountByInvoiceIds(invoiceIds);

        // Then: Should return zero without querying
        assertThat(result).isEqualByComparingTo("0.00");
    }

    // ==================== deleteById() Tests ====================

    @Test
    @DisplayName("deleteById() - Should delegate to repository")
    void shouldDelegateDeleteToRepository() {
        // Given
        Long paymentId = 1L;

        // When
        adapter.deleteById(paymentId);

        // Then
        verify(repository).deleteById(paymentId);
    }

    // ==================== Mapping Tests ====================

    @Test
    @DisplayName("Mapping - Should preserve LocalDateTime precision for payment date")
    void shouldPreserveLocalDateTimePrecisionForPaymentDate() {
        // Given: Payment with specific date and time
        LocalDateTime paymentDate = LocalDateTime.of(2025, 6, 15, 14, 30, 45);
        PartialPayment domainPayment = TestDataBuilder.createPartialPayment(
                null, 1L, new BigDecimal("1000.00"), paymentDate);

        PartialPaymentEntity savedEntity = new PartialPaymentEntity();
        savedEntity.setId(1L);
        savedEntity.setInvoiceId(1L);
        savedEntity.setAmount(new BigDecimal("1000.00"));
        savedEntity.setPaymentDate(paymentDate);
        savedEntity.setCreatedAt(LocalDateTime.now());

        when(repository.save(any(PartialPaymentEntity.class))).thenReturn(savedEntity);

        // When
        PartialPayment result = adapter.save(domainPayment);

        // Then: DateTime preserved exactly
        assertThat(result.getPaymentDate()).isEqualTo(paymentDate);
    }

    @Test
    @DisplayName("Mapping - Should handle entity with ID for updates")
    void shouldHandleEntityWithIdForUpdates() {
        // Given: PartialPayment with existing ID
        PartialPayment domainPayment = TestDataBuilder.createPartialPayment(
                10L, 5L, new BigDecimal("1500.00"), LocalDateTime.now());

        PartialPaymentEntity savedEntity = new PartialPaymentEntity();
        savedEntity.setId(10L);
        savedEntity.setInvoiceId(5L);
        savedEntity.setAmount(new BigDecimal("1500.00"));
        savedEntity.setPaymentDate(domainPayment.getPaymentDate());
        savedEntity.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));

        when(repository.save(any(PartialPaymentEntity.class))).thenReturn(savedEntity);

        // When
        PartialPayment result = adapter.save(domainPayment);

        // Then: ID preserved in mapping
        verify(repository).save(entityCaptor.capture());
        PartialPaymentEntity capturedEntity = entityCaptor.getValue();
        assertThat(capturedEntity.getId()).isEqualTo(10L);

        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Mapping - Should correctly map BigDecimal precision")
    void shouldCorrectlyMapBigDecimalPrecision() {
        // Given: Payment with precise amount
        PartialPayment domainPayment = TestDataBuilder.createPartialPayment(
                null, 1L, new BigDecimal("1234.56"), LocalDateTime.now());

        PartialPaymentEntity savedEntity = new PartialPaymentEntity();
        savedEntity.setId(1L);
        savedEntity.setInvoiceId(1L);
        savedEntity.setAmount(new BigDecimal("1234.56"));
        savedEntity.setPaymentDate(domainPayment.getPaymentDate());
        savedEntity.setCreatedAt(LocalDateTime.now());

        when(repository.save(any(PartialPaymentEntity.class))).thenReturn(savedEntity);

        // When
        PartialPayment result = adapter.save(domainPayment);

        // Then: Precision preserved
        assertThat(result.getAmount()).isEqualByComparingTo("1234.56");
    }
}
