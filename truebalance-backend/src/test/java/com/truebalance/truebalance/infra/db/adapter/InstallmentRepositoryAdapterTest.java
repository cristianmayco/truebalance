package com.truebalance.truebalance.infra.db.adapter;

import com.truebalance.truebalance.domain.entity.Installment;
import com.truebalance.truebalance.infra.db.entity.InstallmentEntity;
import com.truebalance.truebalance.infra.db.repository.InstallmentRepository;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for InstallmentRepositoryAdapter.
 * Tests mapping, batch operations, and aggregation queries.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InstallmentRepositoryAdapter Tests")
class InstallmentRepositoryAdapterTest {

    @Mock
    private InstallmentRepository repository;

    @InjectMocks
    private InstallmentRepositoryAdapter adapter;

    @Captor
    private ArgumentCaptor<InstallmentEntity> entityCaptor;

    @Captor
    private ArgumentCaptor<List<InstallmentEntity>> entitiesCaptor;

    // ==================== save() Tests ====================

    @Test
    @DisplayName("save() - Should map domain to entity and save")
    void shouldMapDomainToEntityAndSave() {
        // Given: Domain Installment
        Installment domainInstallment = TestDataBuilder.createInstallment(
                null, 1L, 1L, 1, new BigDecimal("100.00"), LocalDate.of(2025, 1, 17));
        domainInstallment.setCreditCardId(1L);

        InstallmentEntity savedEntity = new InstallmentEntity();
        savedEntity.setId(1L);
        savedEntity.setBillId(1L);
        savedEntity.setCreditCardId(1L);
        savedEntity.setInvoiceId(1L);
        savedEntity.setInstallmentNumber(1);
        savedEntity.setAmount(new BigDecimal("100.00"));
        savedEntity.setDueDate(LocalDate.of(2025, 1, 17));
        savedEntity.setCreatedAt(LocalDateTime.now());

        when(repository.save(any(InstallmentEntity.class))).thenReturn(savedEntity);

        // When
        Installment result = adapter.save(domainInstallment);

        // Then: Should map correctly
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getBillId()).isEqualTo(1L);
        assertThat(result.getCreditCardId()).isEqualTo(1L);
        assertThat(result.getInvoiceId()).isEqualTo(1L);
        assertThat(result.getInstallmentNumber()).isEqualTo(1);
        assertThat(result.getAmount()).isEqualByComparingTo("100.00");
        assertThat(result.getDueDate()).isEqualTo(LocalDate.of(2025, 1, 17));
        assertThat(result.getCreatedAt()).isNotNull();

        // Verify entity mapping
        verify(repository).save(entityCaptor.capture());
        InstallmentEntity capturedEntity = entityCaptor.getValue();
        assertThat(capturedEntity.getBillId()).isEqualTo(1L);
        assertThat(capturedEntity.getAmount()).isEqualByComparingTo("100.00");
    }

    // ==================== saveAll() Tests ====================

    @Test
    @DisplayName("saveAll() - Should map and save multiple installments")
    void shouldMapAndSaveMultipleInstallments() {
        // Given: Multiple domain installments
        Installment inst1 = TestDataBuilder.createInstallment(null, 1L, 1L, 1, new BigDecimal("100.00"), LocalDate.of(2025, 1, 17));
        inst1.setCreditCardId(1L);
        Installment inst2 = TestDataBuilder.createInstallment(null, 1L, 2L, 2, new BigDecimal("100.00"), LocalDate.of(2025, 2, 17));
        inst2.setCreditCardId(1L);
        Installment inst3 = TestDataBuilder.createInstallment(null, 1L, 3L, 3, new BigDecimal("100.00"), LocalDate.of(2025, 3, 17));
        inst3.setCreditCardId(1L);

        List<Installment> domainInstallments = List.of(inst1, inst2, inst3);

        InstallmentEntity savedEntity1 = new InstallmentEntity();
        savedEntity1.setId(1L);
        savedEntity1.setBillId(1L);
        savedEntity1.setInvoiceId(1L);
        savedEntity1.setInstallmentNumber(1);
        savedEntity1.setAmount(new BigDecimal("100.00"));
        savedEntity1.setDueDate(LocalDate.of(2025, 1, 17));
        savedEntity1.setCreatedAt(LocalDateTime.now());

        InstallmentEntity savedEntity2 = new InstallmentEntity();
        savedEntity2.setId(2L);
        savedEntity2.setBillId(1L);
        savedEntity2.setInvoiceId(2L);
        savedEntity2.setInstallmentNumber(2);
        savedEntity2.setAmount(new BigDecimal("100.00"));
        savedEntity2.setDueDate(LocalDate.of(2025, 2, 17));
        savedEntity2.setCreatedAt(LocalDateTime.now());

        InstallmentEntity savedEntity3 = new InstallmentEntity();
        savedEntity3.setId(3L);
        savedEntity3.setBillId(1L);
        savedEntity3.setInvoiceId(3L);
        savedEntity3.setInstallmentNumber(3);
        savedEntity3.setAmount(new BigDecimal("100.00"));
        savedEntity3.setDueDate(LocalDate.of(2025, 3, 17));
        savedEntity3.setCreatedAt(LocalDateTime.now());

        when(repository.saveAll(anyList())).thenReturn(List.of(savedEntity1, savedEntity2, savedEntity3));

        // When
        List<Installment> result = adapter.saveAll(domainInstallments);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getInstallmentNumber()).isEqualTo(1);
        assertThat(result.get(1).getInstallmentNumber()).isEqualTo(2);
        assertThat(result.get(2).getInstallmentNumber()).isEqualTo(3);

        // Verify batch save
        verify(repository).saveAll(entitiesCaptor.capture());
        assertThat(entitiesCaptor.getValue()).hasSize(3);
    }

    // ==================== findByBillId() Tests ====================

    @Test
    @DisplayName("findByBillId() - Should return installments ordered by installment number")
    void shouldReturnInstallmentsOrderedByNumber() {
        // Given: Bill with multiple installments
        Long billId = 1L;

        InstallmentEntity entity1 = new InstallmentEntity();
        entity1.setId(1L);
        entity1.setBillId(billId);
        entity1.setInstallmentNumber(1);
        entity1.setAmount(new BigDecimal("100.00"));
        entity1.setDueDate(LocalDate.of(2025, 1, 17));
        entity1.setCreatedAt(LocalDateTime.now());

        InstallmentEntity entity2 = new InstallmentEntity();
        entity2.setId(2L);
        entity2.setBillId(billId);
        entity2.setInstallmentNumber(2);
        entity2.setAmount(new BigDecimal("100.00"));
        entity2.setDueDate(LocalDate.of(2025, 2, 17));
        entity2.setCreatedAt(LocalDateTime.now());

        when(repository.findByBillIdOrderByInstallmentNumberAsc(billId))
                .thenReturn(List.of(entity1, entity2));

        // When
        List<Installment> result = adapter.findByBillId(billId);

        // Then: Ordered by installment number
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getInstallmentNumber()).isEqualTo(1);
        assertThat(result.get(1).getInstallmentNumber()).isEqualTo(2);
    }

    @Test
    @DisplayName("findByBillId() - Should return empty list when no installments exist")
    void shouldReturnEmptyListWhenNoInstallmentsForBill() {
        // Given: No installments for bill
        Long billId = 999L;
        when(repository.findByBillIdOrderByInstallmentNumberAsc(billId))
                .thenReturn(List.of());

        // When
        List<Installment> result = adapter.findByBillId(billId);

        // Then
        assertThat(result).isEmpty();
    }

    // ==================== findByInvoiceId() Tests ====================

    @Test
    @DisplayName("findByInvoiceId() - Should return installments ordered by due date")
    void shouldReturnInstallmentsOrderedByDueDate() {
        // Given: Invoice with multiple installments
        Long invoiceId = 1L;

        InstallmentEntity entity1 = new InstallmentEntity();
        entity1.setId(1L);
        entity1.setInvoiceId(invoiceId);
        entity1.setInstallmentNumber(1);
        entity1.setAmount(new BigDecimal("200.00"));
        entity1.setDueDate(LocalDate.of(2025, 1, 10));
        entity1.setCreatedAt(LocalDateTime.now());

        InstallmentEntity entity2 = new InstallmentEntity();
        entity2.setId(2L);
        entity2.setInvoiceId(invoiceId);
        entity2.setInstallmentNumber(2);
        entity2.setAmount(new BigDecimal("150.00"));
        entity2.setDueDate(LocalDate.of(2025, 1, 15));
        entity2.setCreatedAt(LocalDateTime.now());

        when(repository.findByInvoiceIdOrderByDueDateAsc(invoiceId))
                .thenReturn(List.of(entity1, entity2));

        // When
        List<Installment> result = adapter.findByInvoiceId(invoiceId);

        // Then: Ordered by due date
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDueDate()).isEqualTo(LocalDate.of(2025, 1, 10));
        assertThat(result.get(1).getDueDate()).isEqualTo(LocalDate.of(2025, 1, 15));
    }

    // ==================== deleteByBillId() Tests ====================

    @Test
    @DisplayName("deleteByBillId() - Should delegate to repository")
    void shouldDelegateDeleteByBillIdToRepository() {
        // Given
        Long billId = 1L;

        // When
        adapter.deleteByBillId(billId);

        // Then
        verify(repository).deleteByBillId(billId);
    }

    // ==================== sumAmountByInvoiceIds() Tests ====================

    @Test
    @DisplayName("sumAmountByInvoiceIds() - Should return sum of installments")
    void shouldReturnSumOfInstallments() {
        // Given: Multiple invoices
        List<Long> invoiceIds = List.of(1L, 2L, 3L);
        BigDecimal totalSum = new BigDecimal("2500.00");

        when(repository.sumAmountByInvoiceIds(invoiceIds)).thenReturn(totalSum);

        // When
        BigDecimal result = adapter.sumAmountByInvoiceIds(invoiceIds);

        // Then
        assertThat(result).isEqualByComparingTo("2500.00");
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

    @Test
    @DisplayName("sumAmountByInvoiceIds() - Should handle single invoice")
    void shouldHandleSingleInvoice() {
        // Given: Single invoice
        List<Long> invoiceIds = List.of(1L);
        BigDecimal sum = new BigDecimal("1000.00");

        when(repository.sumAmountByInvoiceIds(invoiceIds)).thenReturn(sum);

        // When
        BigDecimal result = adapter.sumAmountByInvoiceIds(invoiceIds);

        // Then
        assertThat(result).isEqualByComparingTo("1000.00");
    }

    // ==================== Mapping Tests ====================

    @Test
    @DisplayName("Mapping - Should preserve all installment fields")
    void shouldPreserveAllInstallmentFields() {
        // Given: Installment with all fields
        Installment domainInstallment = TestDataBuilder.createInstallment(
                5L, 10L, 20L, 3, new BigDecimal("333.33"), LocalDate.of(2025, 3, 17));
        domainInstallment.setCreditCardId(1L);

        InstallmentEntity savedEntity = new InstallmentEntity();
        savedEntity.setId(5L);
        savedEntity.setBillId(10L);
        savedEntity.setCreditCardId(1L);
        savedEntity.setInvoiceId(20L);
        savedEntity.setInstallmentNumber(3);
        savedEntity.setAmount(new BigDecimal("333.33"));
        savedEntity.setDueDate(LocalDate.of(2025, 3, 17));
        savedEntity.setCreatedAt(LocalDateTime.now());

        when(repository.save(any(InstallmentEntity.class))).thenReturn(savedEntity);

        // When
        Installment result = adapter.save(domainInstallment);

        // Then: All fields preserved
        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getBillId()).isEqualTo(10L);
        assertThat(result.getCreditCardId()).isEqualTo(1L);
        assertThat(result.getInvoiceId()).isEqualTo(20L);
        assertThat(result.getInstallmentNumber()).isEqualTo(3);
        assertThat(result.getAmount()).isEqualByComparingTo("333.33");
        assertThat(result.getDueDate()).isEqualTo(LocalDate.of(2025, 3, 17));
    }

    @Test
    @DisplayName("Mapping - Should correctly map LocalDate for due date")
    void shouldCorrectlyMapLocalDateForDueDate() {
        // Given: Specific due date
        LocalDate dueDate = LocalDate.of(2025, 12, 25);
        Installment domainInstallment = TestDataBuilder.createInstallment(
                null, 1L, 1L, 1, new BigDecimal("500.00"), dueDate);

        InstallmentEntity savedEntity = new InstallmentEntity();
        savedEntity.setId(1L);
        savedEntity.setBillId(1L);
        savedEntity.setInvoiceId(1L);
        savedEntity.setInstallmentNumber(1);
        savedEntity.setAmount(new BigDecimal("500.00"));
        savedEntity.setDueDate(dueDate);
        savedEntity.setCreatedAt(LocalDateTime.now());

        when(repository.save(any(InstallmentEntity.class))).thenReturn(savedEntity);

        // When
        Installment result = adapter.save(domainInstallment);

        // Then: Date preserved exactly
        assertThat(result.getDueDate()).isEqualTo(dueDate);
    }
}
