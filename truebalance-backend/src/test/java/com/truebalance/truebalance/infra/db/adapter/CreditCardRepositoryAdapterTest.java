package com.truebalance.truebalance.infra.db.adapter;

import com.truebalance.truebalance.domain.entity.CreditCard;
import com.truebalance.truebalance.infra.db.entity.CreditCardEntity;
import com.truebalance.truebalance.infra.db.repository.CreditCardRepository;
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
 * Unit tests for CreditCardRepositoryAdapter.
 * Tests mapping between domain and JPA entities and delegation to Spring Data repository.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CreditCardRepositoryAdapter Tests")
class CreditCardRepositoryAdapterTest {

    @Mock
    private CreditCardRepository repository;

    @InjectMocks
    private CreditCardRepositoryAdapter adapter;

    @Captor
    private ArgumentCaptor<CreditCardEntity> entityCaptor;

    // ==================== save() Tests ====================

    @Test
    @DisplayName("save() - Should map domain to entity and save")
    void shouldMapDomainToEntityAndSave() {
        // Given: Domain CreditCard
        CreditCard domainCard = TestDataBuilder.createCreditCard(null, "Visa Gold", new BigDecimal("5000.00"), 10, 17);

        CreditCardEntity savedEntity = new CreditCardEntity();
        savedEntity.setId(1L);
        savedEntity.setName("Visa Gold");
        savedEntity.setCreditLimit(new BigDecimal("5000.00"));
        savedEntity.setClosingDay(10);
        savedEntity.setDueDay(17);
        savedEntity.setAllowsPartialPayment(true);
        savedEntity.setCreatedAt(LocalDateTime.now());
        savedEntity.setUpdatedAt(LocalDateTime.now());

        when(repository.save(any(CreditCardEntity.class))).thenReturn(savedEntity);

        // When
        CreditCard result = adapter.save(domainCard);

        // Then: Should map correctly
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Visa Gold");
        assertThat(result.getCreditLimit()).isEqualByComparingTo("5000.00");
        assertThat(result.getClosingDay()).isEqualTo(10);
        assertThat(result.getDueDay()).isEqualTo(17);
        assertThat(result.isAllowsPartialPayment()).isTrue();
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();

        // Verify entity mapping
        verify(repository).save(entityCaptor.capture());
        CreditCardEntity capturedEntity = entityCaptor.getValue();
        assertThat(capturedEntity.getName()).isEqualTo("Visa Gold");
        assertThat(capturedEntity.getCreditLimit()).isEqualByComparingTo("5000.00");
    }

    @Test
    @DisplayName("save() - Should preserve allowsPartialPayment flag")
    void shouldPreserveAllowsPartialPaymentFlag() {
        // Given: Card with allowsPartialPayment = false
        CreditCard domainCard = TestDataBuilder.createCreditCard(null, "Restricted Card", new BigDecimal("3000.00"), 5, 12);
        domainCard.setAllowsPartialPayment(false);

        CreditCardEntity savedEntity = new CreditCardEntity();
        savedEntity.setId(1L);
        savedEntity.setName("Restricted Card");
        savedEntity.setCreditLimit(new BigDecimal("3000.00"));
        savedEntity.setClosingDay(5);
        savedEntity.setDueDay(12);
        savedEntity.setAllowsPartialPayment(false);
        savedEntity.setCreatedAt(LocalDateTime.now());
        savedEntity.setUpdatedAt(LocalDateTime.now());

        when(repository.save(any(CreditCardEntity.class))).thenReturn(savedEntity);

        // When
        CreditCard result = adapter.save(domainCard);

        // Then
        assertThat(result.isAllowsPartialPayment()).isFalse();

        verify(repository).save(entityCaptor.capture());
        assertThat(entityCaptor.getValue().isAllowsPartialPayment()).isFalse();
    }

    @Test
    @DisplayName("save() - Should handle boundary day values (1 and 31)")
    void shouldHandleBoundaryDayValues() {
        // Given: Card with boundary values
        CreditCard domainCard = TestDataBuilder.createCreditCard(null, "Boundary Card", new BigDecimal("8000.00"), 1, 31);

        CreditCardEntity savedEntity = new CreditCardEntity();
        savedEntity.setId(1L);
        savedEntity.setName("Boundary Card");
        savedEntity.setCreditLimit(new BigDecimal("8000.00"));
        savedEntity.setClosingDay(1);
        savedEntity.setDueDay(31);
        savedEntity.setAllowsPartialPayment(true);
        savedEntity.setCreatedAt(LocalDateTime.now());
        savedEntity.setUpdatedAt(LocalDateTime.now());

        when(repository.save(any(CreditCardEntity.class))).thenReturn(savedEntity);

        // When
        CreditCard result = adapter.save(domainCard);

        // Then
        assertThat(result.getClosingDay()).isEqualTo(1);
        assertThat(result.getDueDay()).isEqualTo(31);
    }

    // ==================== findById() Tests ====================

    @Test
    @DisplayName("findById() - Should return mapped domain entity when found")
    void shouldReturnMappedDomainEntityWhenFound() {
        // Given: Entity exists
        Long cardId = 1L;
        CreditCardEntity entity = new CreditCardEntity();
        entity.setId(cardId);
        entity.setName("Found Card");
        entity.setCreditLimit(new BigDecimal("10000.00"));
        entity.setClosingDay(15);
        entity.setDueDay(22);
        entity.setAllowsPartialPayment(true);
        entity.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        entity.setUpdatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));

        when(repository.findById(cardId)).thenReturn(Optional.of(entity));

        // When
        Optional<CreditCard> result = adapter.findById(cardId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(cardId);
        assertThat(result.get().getName()).isEqualTo("Found Card");
        assertThat(result.get().getCreditLimit()).isEqualByComparingTo("10000.00");
        assertThat(result.get().getCreatedAt()).isEqualTo(LocalDateTime.of(2025, 1, 1, 10, 0));
    }

    @Test
    @DisplayName("findById() - Should return empty Optional when not found")
    void shouldReturnEmptyOptionalWhenNotFound() {
        // Given: Entity does not exist
        Long cardId = 999L;
        when(repository.findById(cardId)).thenReturn(Optional.empty());

        // When
        Optional<CreditCard> result = adapter.findById(cardId);

        // Then
        assertThat(result).isEmpty();
    }

    // ==================== findAll() Tests ====================

    @Test
    @DisplayName("findAll() - Should return all cards mapped to domain")
    void shouldReturnAllCardsMappedToDomain() {
        // Given: Multiple entities
        CreditCardEntity entity1 = new CreditCardEntity();
        entity1.setId(1L);
        entity1.setName("Visa");
        entity1.setCreditLimit(new BigDecimal("5000.00"));
        entity1.setClosingDay(10);
        entity1.setDueDay(17);
        entity1.setAllowsPartialPayment(true);
        entity1.setCreatedAt(LocalDateTime.now());
        entity1.setUpdatedAt(LocalDateTime.now());

        CreditCardEntity entity2 = new CreditCardEntity();
        entity2.setId(2L);
        entity2.setName("Mastercard");
        entity2.setCreditLimit(new BigDecimal("8000.00"));
        entity2.setClosingDay(5);
        entity2.setDueDay(12);
        entity2.setAllowsPartialPayment(false);
        entity2.setCreatedAt(LocalDateTime.now());
        entity2.setUpdatedAt(LocalDateTime.now());

        when(repository.findAll()).thenReturn(List.of(entity1, entity2));

        // When
        List<CreditCard> result = adapter.findAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getName()).isEqualTo("Visa");
        assertThat(result.get(0).isAllowsPartialPayment()).isTrue();
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getName()).isEqualTo("Mastercard");
        assertThat(result.get(1).isAllowsPartialPayment()).isFalse();
    }

    @Test
    @DisplayName("findAll() - Should return empty list when no cards exist")
    void shouldReturnEmptyListWhenNoCardsExist() {
        // Given: No entities
        when(repository.findAll()).thenReturn(List.of());

        // When
        List<CreditCard> result = adapter.findAll();

        // Then
        assertThat(result).isEmpty();
    }

    // ==================== deleteById() Tests ====================

    @Test
    @DisplayName("deleteById() - Should delegate to repository")
    void shouldDelegateDeleteToRepository() {
        // Given
        Long cardId = 1L;

        // When
        adapter.deleteById(cardId);

        // Then
        verify(repository).deleteById(cardId);
    }

    // ==================== Mapping Tests ====================

    @Test
    @DisplayName("Mapping - Should correctly map BigDecimal precision for credit limit")
    void shouldCorrectlyMapBigDecimalPrecisionForCreditLimit() {
        // Given: Card with precise limit
        CreditCard domainCard = TestDataBuilder.createCreditCard(null, "Precision Test", new BigDecimal("12345.67"), 10, 17);

        CreditCardEntity savedEntity = new CreditCardEntity();
        savedEntity.setId(1L);
        savedEntity.setName("Precision Test");
        savedEntity.setCreditLimit(new BigDecimal("12345.67"));
        savedEntity.setClosingDay(10);
        savedEntity.setDueDay(17);
        savedEntity.setAllowsPartialPayment(true);
        savedEntity.setCreatedAt(LocalDateTime.now());
        savedEntity.setUpdatedAt(LocalDateTime.now());

        when(repository.save(any(CreditCardEntity.class))).thenReturn(savedEntity);

        // When
        CreditCard result = adapter.save(domainCard);

        // Then: Precision preserved
        assertThat(result.getCreditLimit()).isEqualByComparingTo("12345.67");
    }

    @Test
    @DisplayName("Mapping - Should handle entity with ID for updates")
    void shouldHandleEntityWithIdForUpdates() {
        // Given: CreditCard with existing ID (update scenario)
        CreditCard domainCard = TestDataBuilder.createCreditCard(10L, "Updated Card", new BigDecimal("15000.00"), 20, 27);

        CreditCardEntity savedEntity = new CreditCardEntity();
        savedEntity.setId(10L);
        savedEntity.setName("Updated Card");
        savedEntity.setCreditLimit(new BigDecimal("15000.00"));
        savedEntity.setClosingDay(20);
        savedEntity.setDueDay(27);
        savedEntity.setAllowsPartialPayment(true);
        savedEntity.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        savedEntity.setUpdatedAt(LocalDateTime.now());

        when(repository.save(any(CreditCardEntity.class))).thenReturn(savedEntity);

        // When
        CreditCard result = adapter.save(domainCard);

        // Then: ID preserved in mapping
        verify(repository).save(entityCaptor.capture());
        CreditCardEntity capturedEntity = entityCaptor.getValue();
        assertThat(capturedEntity.getId()).isEqualTo(10L);

        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Mapping - Should correctly map all integer fields")
    void shouldCorrectlyMapAllIntegerFields() {
        // Given: Card with specific day values
        CreditCard domainCard = TestDataBuilder.createCreditCard(null, "Day Test", new BigDecimal("5000.00"), 25, 7);

        CreditCardEntity savedEntity = new CreditCardEntity();
        savedEntity.setId(1L);
        savedEntity.setName("Day Test");
        savedEntity.setCreditLimit(new BigDecimal("5000.00"));
        savedEntity.setClosingDay(25);
        savedEntity.setDueDay(7);
        savedEntity.setAllowsPartialPayment(true);
        savedEntity.setCreatedAt(LocalDateTime.now());
        savedEntity.setUpdatedAt(LocalDateTime.now());

        when(repository.save(any(CreditCardEntity.class))).thenReturn(savedEntity);

        // When
        CreditCard result = adapter.save(domainCard);

        // Then: All integer fields mapped correctly
        assertThat(result.getClosingDay()).isEqualTo(25);
        assertThat(result.getDueDay()).isEqualTo(7);
    }
}
