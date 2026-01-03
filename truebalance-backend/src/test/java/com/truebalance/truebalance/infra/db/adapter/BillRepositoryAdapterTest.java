package com.truebalance.truebalance.infra.db.adapter;

import com.truebalance.truebalance.domain.entity.Bill;
import com.truebalance.truebalance.infra.db.entity.BillEntity;
import com.truebalance.truebalance.infra.db.repository.BillRepository;
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
 * Unit tests for BillRepositoryAdapter.
 * Tests mapping between domain and JPA entities and delegation to Spring Data repository.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BillRepositoryAdapter Tests")
class BillRepositoryAdapterTest {

    @Mock
    private BillRepository repository;

    @InjectMocks
    private BillRepositoryAdapter adapter;

    @Captor
    private ArgumentCaptor<BillEntity> entityCaptor;

    // ==================== save() Tests ====================

    @Test
    @DisplayName("save() - Should map domain to entity and save")
    void shouldMapDomainToEntityAndSave() {
        // Given: Domain Bill
        Bill domainBill = TestDataBuilder.createBill(null, "Test Bill", new BigDecimal("1000.00"), 10);
        domainBill.setInstallmentAmount(new BigDecimal("100.00"));

        BillEntity savedEntity = new BillEntity();
        savedEntity.setId(1L);
        savedEntity.setName("Test Bill");
        savedEntity.setExecutionDate(domainBill.getExecutionDate());
        savedEntity.setTotalAmount(new BigDecimal("1000.00"));
        savedEntity.setNumberOfInstallments(10);
        savedEntity.setInstallmentAmount(new BigDecimal("100.00"));
        savedEntity.setCreatedAt(LocalDateTime.now());
        savedEntity.setUpdatedAt(LocalDateTime.now());

        when(repository.save(any(BillEntity.class))).thenReturn(savedEntity);

        // When
        Bill result = adapter.save(domainBill);

        // Then: Should map correctly
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Bill");
        assertThat(result.getTotalAmount()).isEqualByComparingTo("1000.00");
        assertThat(result.getNumberOfInstallments()).isEqualTo(10);
        assertThat(result.getInstallmentAmount()).isEqualByComparingTo("100.00");
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();

        // Verify entity mapping
        verify(repository).save(entityCaptor.capture());
        BillEntity capturedEntity = entityCaptor.getValue();
        assertThat(capturedEntity.getName()).isEqualTo("Test Bill");
        assertThat(capturedEntity.getTotalAmount()).isEqualByComparingTo("1000.00");
    }

    @Test
    @DisplayName("save() - Should preserve all fields during mapping")
    void shouldPreserveAllFieldsDuringMapping() {
        // Given: Bill with all fields set
        Bill domainBill = TestDataBuilder.createBill(5L, "Complete Bill", new BigDecimal("2500.00"), 12);
        domainBill.setInstallmentAmount(new BigDecimal("208.33"));
        domainBill.setDescription("Detailed description");

        BillEntity savedEntity = new BillEntity();
        savedEntity.setId(5L);
        savedEntity.setName("Complete Bill");
        savedEntity.setExecutionDate(domainBill.getExecutionDate());
        savedEntity.setTotalAmount(new BigDecimal("2500.00"));
        savedEntity.setNumberOfInstallments(12);
        savedEntity.setInstallmentAmount(new BigDecimal("208.33"));
        savedEntity.setDescription("Detailed description");
        savedEntity.setCreatedAt(LocalDateTime.now());
        savedEntity.setUpdatedAt(LocalDateTime.now());

        when(repository.save(any(BillEntity.class))).thenReturn(savedEntity);

        // When
        Bill result = adapter.save(domainBill);

        // Then: All fields preserved
        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getDescription()).isEqualTo("Detailed description");
        assertThat(result.getInstallmentAmount()).isEqualByComparingTo("208.33");
    }

    @Test
    @DisplayName("save() - Should handle null description")
    void shouldHandleNullDescription() {
        // Given: Bill without description
        Bill domainBill = TestDataBuilder.createBill(null, "No Description Bill", new BigDecimal("500.00"), 5);
        domainBill.setDescription(null);

        BillEntity savedEntity = new BillEntity();
        savedEntity.setId(1L);
        savedEntity.setName("No Description Bill");
        savedEntity.setExecutionDate(domainBill.getExecutionDate());
        savedEntity.setTotalAmount(new BigDecimal("500.00"));
        savedEntity.setNumberOfInstallments(5);
        savedEntity.setDescription(null);
        savedEntity.setCreatedAt(LocalDateTime.now());
        savedEntity.setUpdatedAt(LocalDateTime.now());

        when(repository.save(any(BillEntity.class))).thenReturn(savedEntity);

        // When
        Bill result = adapter.save(domainBill);

        // Then: Should handle null
        assertThat(result.getDescription()).isNull();
    }

    // ==================== findById() Tests ====================

    @Test
    @DisplayName("findById() - Should return mapped domain entity when found")
    void shouldReturnMappedDomainEntityWhenFound() {
        // Given: Entity exists
        Long billId = 1L;
        BillEntity entity = new BillEntity();
        entity.setId(billId);
        entity.setName("Found Bill");
        entity.setExecutionDate(LocalDateTime.of(2025, 1, 15, 10, 0));
        entity.setTotalAmount(new BigDecimal("1500.00"));
        entity.setNumberOfInstallments(15);
        entity.setInstallmentAmount(new BigDecimal("100.00"));
        entity.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        entity.setUpdatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));

        when(repository.findById(billId)).thenReturn(Optional.of(entity));

        // When
        Optional<Bill> result = adapter.findById(billId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(billId);
        assertThat(result.get().getName()).isEqualTo("Found Bill");
        assertThat(result.get().getTotalAmount()).isEqualByComparingTo("1500.00");
        assertThat(result.get().getCreatedAt()).isEqualTo(LocalDateTime.of(2025, 1, 1, 10, 0));
    }

    @Test
    @DisplayName("findById() - Should return empty Optional when not found")
    void shouldReturnEmptyOptionalWhenNotFound() {
        // Given: Entity does not exist
        Long billId = 999L;
        when(repository.findById(billId)).thenReturn(Optional.empty());

        // When
        Optional<Bill> result = adapter.findById(billId);

        // Then
        assertThat(result).isEmpty();
    }

    // ==================== findAll() Tests ====================

    @Test
    @DisplayName("findAll() - Should return all bills mapped to domain")
    void shouldReturnAllBillsMappedToDomain() {
        // Given: Multiple entities
        BillEntity entity1 = new BillEntity();
        entity1.setId(1L);
        entity1.setName("Bill 1");
        entity1.setExecutionDate(LocalDateTime.now());
        entity1.setTotalAmount(new BigDecimal("1000.00"));
        entity1.setNumberOfInstallments(10);
        entity1.setCreatedAt(LocalDateTime.now());
        entity1.setUpdatedAt(LocalDateTime.now());

        BillEntity entity2 = new BillEntity();
        entity2.setId(2L);
        entity2.setName("Bill 2");
        entity2.setExecutionDate(LocalDateTime.now());
        entity2.setTotalAmount(new BigDecimal("500.00"));
        entity2.setNumberOfInstallments(5);
        entity2.setCreatedAt(LocalDateTime.now());
        entity2.setUpdatedAt(LocalDateTime.now());

        when(repository.findAll()).thenReturn(List.of(entity1, entity2));

        // When
        List<Bill> result = adapter.findAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getName()).isEqualTo("Bill 1");
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getName()).isEqualTo("Bill 2");
    }

    @Test
    @DisplayName("findAll() - Should return empty list when no bills exist")
    void shouldReturnEmptyListWhenNoBillsExist() {
        // Given: No entities
        when(repository.findAll()).thenReturn(List.of());

        // When
        List<Bill> result = adapter.findAll();

        // Then
        assertThat(result).isEmpty();
    }

    // ==================== deleteById() Tests ====================

    @Test
    @DisplayName("deleteById() - Should delegate to repository")
    void shouldDelegateDeleteToRepository() {
        // Given
        Long billId = 1L;

        // When
        adapter.deleteById(billId);

        // Then
        verify(repository).deleteById(billId);
    }

    // ==================== Mapping Tests ====================

    @Test
    @DisplayName("Mapping - Should correctly map BigDecimal precision")
    void shouldCorrectlyMapBigDecimalPrecision() {
        // Given: Bill with precise decimal values
        Bill domainBill = TestDataBuilder.createBill(null, "Precision Test", new BigDecimal("333.33"), 3);
        domainBill.setInstallmentAmount(new BigDecimal("111.11"));

        BillEntity savedEntity = new BillEntity();
        savedEntity.setId(1L);
        savedEntity.setName("Precision Test");
        savedEntity.setExecutionDate(domainBill.getExecutionDate());
        savedEntity.setTotalAmount(new BigDecimal("333.33"));
        savedEntity.setNumberOfInstallments(3);
        savedEntity.setInstallmentAmount(new BigDecimal("111.11"));
        savedEntity.setCreatedAt(LocalDateTime.now());
        savedEntity.setUpdatedAt(LocalDateTime.now());

        when(repository.save(any(BillEntity.class))).thenReturn(savedEntity);

        // When
        Bill result = adapter.save(domainBill);

        // Then: Precision preserved
        assertThat(result.getTotalAmount()).isEqualByComparingTo("333.33");
        assertThat(result.getInstallmentAmount()).isEqualByComparingTo("111.11");
    }

    @Test
    @DisplayName("Mapping - Should correctly map LocalDateTime fields")
    void shouldCorrectlyMapLocalDateTimeFields() {
        // Given: Specific execution date
        LocalDateTime specificDate = LocalDateTime.of(2025, 6, 15, 14, 30);
        Bill domainBill = TestDataBuilder.createBill(null, "Date Test", new BigDecimal("1000.00"), 10);
        domainBill.setExecutionDate(specificDate);

        BillEntity savedEntity = new BillEntity();
        savedEntity.setId(1L);
        savedEntity.setName("Date Test");
        savedEntity.setExecutionDate(specificDate);
        savedEntity.setTotalAmount(new BigDecimal("1000.00"));
        savedEntity.setNumberOfInstallments(10);
        savedEntity.setCreatedAt(LocalDateTime.now());
        savedEntity.setUpdatedAt(LocalDateTime.now());

        when(repository.save(any(BillEntity.class))).thenReturn(savedEntity);

        // When
        Bill result = adapter.save(domainBill);

        // Then: Date preserved
        assertThat(result.getExecutionDate()).isEqualTo(specificDate);
    }

    @Test
    @DisplayName("Mapping - Should handle entity with ID for updates")
    void shouldHandleEntityWithIdForUpdates() {
        // Given: Bill with existing ID (update scenario)
        Bill domainBill = TestDataBuilder.createBill(10L, "Updated Bill", new BigDecimal("2000.00"), 20);

        BillEntity savedEntity = new BillEntity();
        savedEntity.setId(10L);
        savedEntity.setName("Updated Bill");
        savedEntity.setExecutionDate(domainBill.getExecutionDate());
        savedEntity.setTotalAmount(new BigDecimal("2000.00"));
        savedEntity.setNumberOfInstallments(20);
        savedEntity.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        savedEntity.setUpdatedAt(LocalDateTime.now());

        when(repository.save(any(BillEntity.class))).thenReturn(savedEntity);

        // When
        Bill result = adapter.save(domainBill);

        // Then: ID preserved in mapping
        verify(repository).save(entityCaptor.capture());
        BillEntity capturedEntity = entityCaptor.getValue();
        assertThat(capturedEntity.getId()).isEqualTo(10L);

        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Mapping - Should correctly map isRecurring field")
    void shouldCorrectlyMapIsRecurringField() {
        // Given: Recurring bill
        Bill domainBill = TestDataBuilder.createBill(null, "Recurring Bill", new BigDecimal("100.00"), 1);
        domainBill.setIsRecurring(true);

        BillEntity savedEntity = new BillEntity();
        savedEntity.setId(1L);
        savedEntity.setName("Recurring Bill");
        savedEntity.setExecutionDate(domainBill.getExecutionDate());
        savedEntity.setTotalAmount(new BigDecimal("100.00"));
        savedEntity.setNumberOfInstallments(1);
        savedEntity.setInstallmentAmount(new BigDecimal("100.00"));
        savedEntity.setIsRecurring(true);
        savedEntity.setCreatedAt(LocalDateTime.now());
        savedEntity.setUpdatedAt(LocalDateTime.now());

        when(repository.save(any(BillEntity.class))).thenReturn(savedEntity);

        // When
        Bill result = adapter.save(domainBill);

        // Then: isRecurring should be mapped correctly
        assertThat(result.getIsRecurring()).isTrue();
        verify(repository).save(entityCaptor.capture());
        BillEntity capturedEntity = entityCaptor.getValue();
        assertThat(capturedEntity.getIsRecurring()).isTrue();
    }

    @Test
    @DisplayName("Mapping - Should correctly map non-recurring bill")
    void shouldCorrectlyMapNonRecurringBill() {
        // Given: Non-recurring bill
        Bill domainBill = TestDataBuilder.createBill(null, "One-time Bill", new BigDecimal("500.00"), 1);
        domainBill.setIsRecurring(false);

        BillEntity savedEntity = new BillEntity();
        savedEntity.setId(1L);
        savedEntity.setName("One-time Bill");
        savedEntity.setExecutionDate(domainBill.getExecutionDate());
        savedEntity.setTotalAmount(new BigDecimal("500.00"));
        savedEntity.setNumberOfInstallments(1);
        savedEntity.setInstallmentAmount(new BigDecimal("500.00"));
        savedEntity.setIsRecurring(false);
        savedEntity.setCreatedAt(LocalDateTime.now());
        savedEntity.setUpdatedAt(LocalDateTime.now());

        when(repository.save(any(BillEntity.class))).thenReturn(savedEntity);

        // When
        Bill result = adapter.save(domainBill);

        // Then: isRecurring should be false
        assertThat(result.getIsRecurring()).isFalse();
        verify(repository).save(entityCaptor.capture());
        BillEntity capturedEntity = entityCaptor.getValue();
        assertThat(capturedEntity.getIsRecurring()).isFalse();
    }

    @Test
    @DisplayName("findById() - Should map isRecurring field from entity")
    void shouldMapIsRecurringFieldFromEntity() {
        // Given: Entity with isRecurring set
        Long billId = 1L;
        BillEntity entity = new BillEntity();
        entity.setId(billId);
        entity.setName("Recurring Bill");
        entity.setExecutionDate(LocalDateTime.of(2025, 1, 15, 10, 0));
        entity.setTotalAmount(new BigDecimal("100.00"));
        entity.setNumberOfInstallments(1);
        entity.setInstallmentAmount(new BigDecimal("100.00"));
        entity.setIsRecurring(true);
        entity.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        entity.setUpdatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));

        when(repository.findById(billId)).thenReturn(Optional.of(entity));

        // When
        Optional<Bill> result = adapter.findById(billId);

        // Then: isRecurring should be mapped
        assertThat(result).isPresent();
        assertThat(result.get().getIsRecurring()).isTrue();
    }

    @Test
    @DisplayName("findAll() - Should map isRecurring field for all bills")
    void shouldMapIsRecurringFieldForAllBills() {
        // Given: Multiple entities with different recurring flags
        BillEntity entity1 = new BillEntity();
        entity1.setId(1L);
        entity1.setName("Recurring Bill");
        entity1.setExecutionDate(LocalDateTime.now());
        entity1.setTotalAmount(new BigDecimal("100.00"));
        entity1.setNumberOfInstallments(1);
        entity1.setIsRecurring(true);
        entity1.setCreatedAt(LocalDateTime.now());
        entity1.setUpdatedAt(LocalDateTime.now());

        BillEntity entity2 = new BillEntity();
        entity2.setId(2L);
        entity2.setName("One-time Bill");
        entity2.setExecutionDate(LocalDateTime.now());
        entity2.setTotalAmount(new BigDecimal("500.00"));
        entity2.setNumberOfInstallments(1);
        entity2.setIsRecurring(false);
        entity2.setCreatedAt(LocalDateTime.now());
        entity2.setUpdatedAt(LocalDateTime.now());

        when(repository.findAll()).thenReturn(List.of(entity1, entity2));

        // When
        List<Bill> result = adapter.findAll();

        // Then: Both recurring flags should be mapped correctly
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getIsRecurring()).isTrue();
        assertThat(result.get(1).getIsRecurring()).isFalse();
    }
}
