package com.truebalance.truebalance.infra.db.adapter;

import com.truebalance.truebalance.domain.entity.Invoice;
import com.truebalance.truebalance.infra.db.entity.InvoiceEntity;
import com.truebalance.truebalance.infra.db.repository.InvoiceRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for InvoiceRepositoryAdapter.
 * Tests mapping between domain and JPA entities and delegation to Spring Data repository.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InvoiceRepositoryAdapter Tests")
class InvoiceRepositoryAdapterTest {

    @Mock
    private InvoiceRepository repository;

    @InjectMocks
    private InvoiceRepositoryAdapter adapter;

    @Captor
    private ArgumentCaptor<InvoiceEntity> entityCaptor;

    @Captor
    private ArgumentCaptor<List<InvoiceEntity>> entitiesCaptor;

    // ==================== save() Tests ====================

    @Test
    @DisplayName("save() - Should map domain to entity and save")
    void shouldMapDomainToEntityAndSave() {
        // Given: Domain Invoice
        Invoice domainInvoice = TestDataBuilder.createInvoice(null, 1L, LocalDate.of(2025, 1, 1), new BigDecimal("1500.00"));
        domainInvoice.setPreviousBalance(new BigDecimal("200.00"));
        domainInvoice.setClosed(false);
        domainInvoice.setPaid(false);

        InvoiceEntity savedEntity = new InvoiceEntity();
        savedEntity.setId(1L);
        savedEntity.setCreditCardId(1L);
        savedEntity.setReferenceMonth(LocalDate.of(2025, 1, 1));
        savedEntity.setTotalAmount(new BigDecimal("1500.00"));
        savedEntity.setPreviousBalance(new BigDecimal("200.00"));
        savedEntity.setClosed(false);
        savedEntity.setPaid(false);
        savedEntity.setCreatedAt(LocalDateTime.now());
        savedEntity.setUpdatedAt(LocalDateTime.now());

        when(repository.save(any(InvoiceEntity.class))).thenReturn(savedEntity);

        // When
        Invoice result = adapter.save(domainInvoice);

        // Then: Should map correctly
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCreditCardId()).isEqualTo(1L);
        assertThat(result.getReferenceMonth()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(result.getTotalAmount()).isEqualByComparingTo("1500.00");
        assertThat(result.getPreviousBalance()).isEqualByComparingTo("200.00");
        assertThat(result.isClosed()).isFalse();
        assertThat(result.isPaid()).isFalse();

        // Verify entity mapping
        verify(repository).save(entityCaptor.capture());
        InvoiceEntity capturedEntity = entityCaptor.getValue();
        assertThat(capturedEntity.getCreditCardId()).isEqualTo(1L);
        assertThat(capturedEntity.getTotalAmount()).isEqualByComparingTo("1500.00");
    }

    @Test
    @DisplayName("save() - Should preserve closed and paid flags")
    void shouldPreserveClosedAndPaidFlags() {
        // Given: Closed and paid invoice
        Invoice domainInvoice = TestDataBuilder.createInvoice(5L, 2L, LocalDate.of(2024, 12, 1), new BigDecimal("2000.00"));
        domainInvoice.setClosed(true);
        domainInvoice.setPaid(true);

        InvoiceEntity savedEntity = new InvoiceEntity();
        savedEntity.setId(5L);
        savedEntity.setCreditCardId(2L);
        savedEntity.setReferenceMonth(LocalDate.of(2024, 12, 1));
        savedEntity.setTotalAmount(new BigDecimal("2000.00"));
        savedEntity.setPreviousBalance(BigDecimal.ZERO);
        savedEntity.setClosed(true);
        savedEntity.setPaid(true);
        savedEntity.setCreatedAt(LocalDateTime.now());
        savedEntity.setUpdatedAt(LocalDateTime.now());

        when(repository.save(any(InvoiceEntity.class))).thenReturn(savedEntity);

        // When
        Invoice result = adapter.save(domainInvoice);

        // Then
        assertThat(result.isClosed()).isTrue();
        assertThat(result.isPaid()).isTrue();
    }

    // ==================== saveAll() Tests ====================

    @Test
    @DisplayName("saveAll() - Should map and save multiple invoices")
    void shouldMapAndSaveMultipleInvoices() {
        // Given: Multiple domain invoices
        Invoice invoice1 = TestDataBuilder.createInvoice(null, 1L, LocalDate.of(2025, 1, 1), new BigDecimal("500.00"));
        Invoice invoice2 = TestDataBuilder.createInvoice(null, 1L, LocalDate.of(2025, 2, 1), new BigDecimal("750.00"));
        Invoice invoice3 = TestDataBuilder.createInvoice(null, 1L, LocalDate.of(2025, 3, 1), new BigDecimal("1000.00"));

        List<Invoice> domainInvoices = List.of(invoice1, invoice2, invoice3);

        InvoiceEntity savedEntity1 = new InvoiceEntity();
        savedEntity1.setId(1L);
        savedEntity1.setCreditCardId(1L);
        savedEntity1.setReferenceMonth(LocalDate.of(2025, 1, 1));
        savedEntity1.setTotalAmount(new BigDecimal("500.00"));
        savedEntity1.setCreatedAt(LocalDateTime.now());
        savedEntity1.setUpdatedAt(LocalDateTime.now());

        InvoiceEntity savedEntity2 = new InvoiceEntity();
        savedEntity2.setId(2L);
        savedEntity2.setCreditCardId(1L);
        savedEntity2.setReferenceMonth(LocalDate.of(2025, 2, 1));
        savedEntity2.setTotalAmount(new BigDecimal("750.00"));
        savedEntity2.setCreatedAt(LocalDateTime.now());
        savedEntity2.setUpdatedAt(LocalDateTime.now());

        InvoiceEntity savedEntity3 = new InvoiceEntity();
        savedEntity3.setId(3L);
        savedEntity3.setCreditCardId(1L);
        savedEntity3.setReferenceMonth(LocalDate.of(2025, 3, 1));
        savedEntity3.setTotalAmount(new BigDecimal("1000.00"));
        savedEntity3.setCreatedAt(LocalDateTime.now());
        savedEntity3.setUpdatedAt(LocalDateTime.now());

        when(repository.saveAll(anyList())).thenReturn(List.of(savedEntity1, savedEntity2, savedEntity3));

        // When
        List<Invoice> result = adapter.saveAll(domainInvoices);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getTotalAmount()).isEqualByComparingTo("500.00");
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getTotalAmount()).isEqualByComparingTo("750.00");
        assertThat(result.get(2).getId()).isEqualTo(3L);
        assertThat(result.get(2).getTotalAmount()).isEqualByComparingTo("1000.00");

        // Verify correct number of entities sent
        verify(repository).saveAll(entitiesCaptor.capture());
        assertThat(entitiesCaptor.getValue()).hasSize(3);
    }

    // ==================== findById() Tests ====================

    @Test
    @DisplayName("findById() - Should return mapped domain entity when found")
    void shouldReturnMappedDomainEntityWhenFound() {
        // Given: Entity exists
        Long invoiceId = 1L;
        InvoiceEntity entity = new InvoiceEntity();
        entity.setId(invoiceId);
        entity.setCreditCardId(1L);
        entity.setReferenceMonth(LocalDate.of(2025, 1, 1));
        entity.setTotalAmount(new BigDecimal("1500.00"));
        entity.setPreviousBalance(new BigDecimal("300.00"));
        entity.setClosed(false);
        entity.setPaid(false);
        entity.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        entity.setUpdatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));

        when(repository.findById(invoiceId)).thenReturn(Optional.of(entity));

        // When
        Optional<Invoice> result = adapter.findById(invoiceId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(invoiceId);
        assertThat(result.get().getTotalAmount()).isEqualByComparingTo("1500.00");
        assertThat(result.get().getPreviousBalance()).isEqualByComparingTo("300.00");
    }

    @Test
    @DisplayName("findById() - Should return empty Optional when not found")
    void shouldReturnEmptyOptionalWhenNotFound() {
        // Given: Entity does not exist
        Long invoiceId = 999L;
        when(repository.findById(invoiceId)).thenReturn(Optional.empty());

        // When
        Optional<Invoice> result = adapter.findById(invoiceId);

        // Then
        assertThat(result).isEmpty();
    }

    // ==================== findByCreditCardIdAndReferenceMonth() Tests ====================

    @Test
    @DisplayName("findByCreditCardIdAndReferenceMonth() - Should return invoice when found")
    void shouldReturnInvoiceWhenFoundByCardAndMonth() {
        // Given: Invoice exists for card and month
        Long creditCardId = 1L;
        LocalDate referenceMonth = LocalDate.of(2025, 1, 1);

        InvoiceEntity entity = new InvoiceEntity();
        entity.setId(1L);
        entity.setCreditCardId(creditCardId);
        entity.setReferenceMonth(referenceMonth);
        entity.setTotalAmount(new BigDecimal("800.00"));
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        when(repository.findByCreditCardIdAndReferenceMonth(creditCardId, referenceMonth))
                .thenReturn(Optional.of(entity));

        // When
        Optional<Invoice> result = adapter.findByCreditCardIdAndReferenceMonth(creditCardId, referenceMonth);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getCreditCardId()).isEqualTo(creditCardId);
        assertThat(result.get().getReferenceMonth()).isEqualTo(referenceMonth);
    }

    @Test
    @DisplayName("findByCreditCardIdAndReferenceMonth() - Should return empty when not found")
    void shouldReturnEmptyWhenNotFoundByCardAndMonth() {
        // Given: No invoice for card and month
        Long creditCardId = 1L;
        LocalDate referenceMonth = LocalDate.of(2025, 6, 1);

        when(repository.findByCreditCardIdAndReferenceMonth(creditCardId, referenceMonth))
                .thenReturn(Optional.empty());

        // When
        Optional<Invoice> result = adapter.findByCreditCardIdAndReferenceMonth(creditCardId, referenceMonth);

        // Then
        assertThat(result).isEmpty();
    }

    // ==================== findByCreditCardId() Tests ====================

    @Test
    @DisplayName("findByCreditCardId() - Should return all invoices for card ordered by month desc")
    void shouldReturnAllInvoicesForCardOrderedByMonthDesc() {
        // Given: Multiple invoices for card
        Long creditCardId = 1L;

        InvoiceEntity entity1 = new InvoiceEntity();
        entity1.setId(1L);
        entity1.setCreditCardId(creditCardId);
        entity1.setReferenceMonth(LocalDate.of(2025, 3, 1));
        entity1.setTotalAmount(new BigDecimal("300.00"));
        entity1.setCreatedAt(LocalDateTime.now());
        entity1.setUpdatedAt(LocalDateTime.now());

        InvoiceEntity entity2 = new InvoiceEntity();
        entity2.setId(2L);
        entity2.setCreditCardId(creditCardId);
        entity2.setReferenceMonth(LocalDate.of(2025, 2, 1));
        entity2.setTotalAmount(new BigDecimal("200.00"));
        entity2.setCreatedAt(LocalDateTime.now());
        entity2.setUpdatedAt(LocalDateTime.now());

        InvoiceEntity entity3 = new InvoiceEntity();
        entity3.setId(3L);
        entity3.setCreditCardId(creditCardId);
        entity3.setReferenceMonth(LocalDate.of(2025, 1, 1));
        entity3.setTotalAmount(new BigDecimal("100.00"));
        entity3.setCreatedAt(LocalDateTime.now());
        entity3.setUpdatedAt(LocalDateTime.now());

        when(repository.findByCreditCardIdOrderByReferenceMonthDesc(creditCardId))
                .thenReturn(List.of(entity1, entity2, entity3));

        // When
        List<Invoice> result = adapter.findByCreditCardId(creditCardId);

        // Then: Ordered by reference month desc (newest first)
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getReferenceMonth()).isEqualTo(LocalDate.of(2025, 3, 1));
        assertThat(result.get(1).getReferenceMonth()).isEqualTo(LocalDate.of(2025, 2, 1));
        assertThat(result.get(2).getReferenceMonth()).isEqualTo(LocalDate.of(2025, 1, 1));
    }

    @Test
    @DisplayName("findByCreditCardId() - Should return empty list when no invoices exist")
    void shouldReturnEmptyListWhenNoInvoicesForCard() {
        // Given: No invoices for card
        Long creditCardId = 999L;
        when(repository.findByCreditCardIdOrderByReferenceMonthDesc(creditCardId))
                .thenReturn(List.of());

        // When
        List<Invoice> result = adapter.findByCreditCardId(creditCardId);

        // Then
        assertThat(result).isEmpty();
    }

    // ==================== findByCreditCardIdAndClosed() Tests ====================

    @Test
    @DisplayName("findByCreditCardIdAndClosed() - Should return only open invoices")
    void shouldReturnOnlyOpenInvoices() {
        // Given: Open invoices (closed = false)
        Long creditCardId = 1L;

        InvoiceEntity openEntity1 = new InvoiceEntity();
        openEntity1.setId(1L);
        openEntity1.setCreditCardId(creditCardId);
        openEntity1.setReferenceMonth(LocalDate.of(2025, 1, 1));
        openEntity1.setTotalAmount(new BigDecimal("500.00"));
        openEntity1.setClosed(false);
        openEntity1.setCreatedAt(LocalDateTime.now());
        openEntity1.setUpdatedAt(LocalDateTime.now());

        InvoiceEntity openEntity2 = new InvoiceEntity();
        openEntity2.setId(2L);
        openEntity2.setCreditCardId(creditCardId);
        openEntity2.setReferenceMonth(LocalDate.of(2025, 2, 1));
        openEntity2.setTotalAmount(new BigDecimal("750.00"));
        openEntity2.setClosed(false);
        openEntity2.setCreatedAt(LocalDateTime.now());
        openEntity2.setUpdatedAt(LocalDateTime.now());

        when(repository.findByCreditCardIdAndClosed(creditCardId, false))
                .thenReturn(List.of(openEntity1, openEntity2));

        // When
        List<Invoice> result = adapter.findByCreditCardIdAndClosed(creditCardId, false);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(invoice -> !invoice.isClosed());
    }

    @Test
    @DisplayName("findByCreditCardIdAndClosed() - Should return only closed invoices")
    void shouldReturnOnlyClosedInvoices() {
        // Given: Closed invoices (closed = true)
        Long creditCardId = 1L;

        InvoiceEntity closedEntity = new InvoiceEntity();
        closedEntity.setId(1L);
        closedEntity.setCreditCardId(creditCardId);
        closedEntity.setReferenceMonth(LocalDate.of(2024, 12, 1));
        closedEntity.setTotalAmount(new BigDecimal("1000.00"));
        closedEntity.setClosed(true);
        closedEntity.setCreatedAt(LocalDateTime.now());
        closedEntity.setUpdatedAt(LocalDateTime.now());

        when(repository.findByCreditCardIdAndClosed(creditCardId, true))
                .thenReturn(List.of(closedEntity));

        // When
        List<Invoice> result = adapter.findByCreditCardIdAndClosed(creditCardId, true);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result).allMatch(Invoice::isClosed);
    }

    // ==================== Mapping Tests ====================

    @Test
    @DisplayName("Mapping - Should correctly map negative previous balance (credit)")
    void shouldCorrectlyMapNegativePreviousBalance() {
        // Given: Invoice with credit from previous month
        Invoice domainInvoice = TestDataBuilder.createInvoice(null, 1L, LocalDate.of(2025, 2, 1), new BigDecimal("1000.00"));
        domainInvoice.setPreviousBalance(new BigDecimal("-300.00"));

        InvoiceEntity savedEntity = new InvoiceEntity();
        savedEntity.setId(1L);
        savedEntity.setCreditCardId(1L);
        savedEntity.setReferenceMonth(LocalDate.of(2025, 2, 1));
        savedEntity.setTotalAmount(new BigDecimal("1000.00"));
        savedEntity.setPreviousBalance(new BigDecimal("-300.00"));
        savedEntity.setCreatedAt(LocalDateTime.now());
        savedEntity.setUpdatedAt(LocalDateTime.now());

        when(repository.save(any(InvoiceEntity.class))).thenReturn(savedEntity);

        // When
        Invoice result = adapter.save(domainInvoice);

        // Then: Negative balance preserved
        assertThat(result.getPreviousBalance()).isEqualByComparingTo("-300.00");
    }

    @Test
    @DisplayName("Mapping - Should correctly map zero amounts")
    void shouldCorrectlyMapZeroAmounts() {
        // Given: Invoice with zero amounts
        Invoice domainInvoice = TestDataBuilder.createInvoice(null, 1L, LocalDate.of(2025, 1, 1), BigDecimal.ZERO);
        domainInvoice.setPreviousBalance(BigDecimal.ZERO);

        InvoiceEntity savedEntity = new InvoiceEntity();
        savedEntity.setId(1L);
        savedEntity.setCreditCardId(1L);
        savedEntity.setReferenceMonth(LocalDate.of(2025, 1, 1));
        savedEntity.setTotalAmount(BigDecimal.ZERO);
        savedEntity.setPreviousBalance(BigDecimal.ZERO);
        savedEntity.setCreatedAt(LocalDateTime.now());
        savedEntity.setUpdatedAt(LocalDateTime.now());

        when(repository.save(any(InvoiceEntity.class))).thenReturn(savedEntity);

        // When
        Invoice result = adapter.save(domainInvoice);

        // Then: Zero amounts preserved
        assertThat(result.getTotalAmount()).isEqualByComparingTo("0.00");
        assertThat(result.getPreviousBalance()).isEqualByComparingTo("0.00");
    }
}
