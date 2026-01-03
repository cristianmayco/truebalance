package com.truebalance.truebalance.domain.usecase.bill;

import com.truebalance.truebalance.domain.entity.Bill;
import com.truebalance.truebalance.domain.port.BillRepositoryPort;
import com.truebalance.truebalance.domain.usecase.GetAllBills;
import com.truebalance.truebalance.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Tests for GetAllBills use case.
 * Simple CRUD operation that retrieves all bills from the repository.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetAllBills - Use Case Tests")
class GetAllBillsTest {

    @Mock
    private BillRepositoryPort repository;

    @InjectMocks
    private GetAllBills getAllBills;

    @Test
    @DisplayName("Should return all bills when repository has data")
    void shouldReturnAllBillsWhenRepositoryHasData() {
        // Given: Repository has 3 bills
        Bill bill1 = TestDataBuilder.createBill(1L, "Bill 1", new BigDecimal("100.00"), 1);
        Bill bill2 = TestDataBuilder.createBill(2L, "Bill 2", new BigDecimal("200.00"), 2);
        Bill bill3 = TestDataBuilder.createBill(3L, "Bill 3", new BigDecimal("300.00"), 3);
        List<Bill> expectedBills = Arrays.asList(bill1, bill2, bill3);

        when(repository.findAll()).thenReturn(expectedBills);

        // When
        List<Bill> result = getAllBills.execute();

        // Then
        assertThat(result)
                .isNotNull()
                .hasSize(3)
                .containsExactly(bill1, bill2, bill3);
    }

    @Test
    @DisplayName("Should return empty list when repository is empty")
    void shouldReturnEmptyListWhenRepositoryIsEmpty() {
        // Given: Repository is empty
        when(repository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<Bill> result = getAllBills.execute();

        // Then
        assertThat(result)
                .isNotNull()
                .isEmpty();
    }

    @Test
    @DisplayName("Should call repository exactly once")
    void shouldCallRepositoryExactlyOnce() {
        // Given
        when(repository.findAll()).thenReturn(Collections.emptyList());

        // When
        getAllBills.execute();

        // Then
        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should map bills correctly")
    void shouldMapBillsCorrectly() {
        // Given: Repository returns a bill with all fields populated
        Bill bill = TestDataBuilder.createBill();
        bill.setId(100L);
        bill.setName("Test Mapping");
        bill.setTotalAmount(new BigDecimal("999.99"));
        bill.setNumberOfInstallments(5);

        when(repository.findAll()).thenReturn(List.of(bill));

        // When
        List<Bill> result = getAllBills.execute();

        // Then: Verify all fields are preserved
        assertThat(result).hasSize(1);
        Bill returnedBill = result.get(0);
        assertThat(returnedBill.getId()).isEqualTo(100L);
        assertThat(returnedBill.getName()).isEqualTo("Test Mapping");
        assertThat(returnedBill.getTotalAmount()).isEqualByComparingTo("999.99");
        assertThat(returnedBill.getNumberOfInstallments()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should handle repository exception")
    void shouldHandleRepositoryException() {
        // Given: Repository throws exception
        when(repository.findAll()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThatThrownBy(() -> getAllBills.execute())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");
    }

    @Test
    @DisplayName("Should return bills in correct order")
    void shouldReturnBillsInCorrectOrder() {
        // Given: Repository returns bills in a specific order
        Bill bill1 = TestDataBuilder.createBill(1L, "First", new BigDecimal("100.00"), 1);
        Bill bill2 = TestDataBuilder.createBill(2L, "Second", new BigDecimal("200.00"), 1);
        Bill bill3 = TestDataBuilder.createBill(3L, "Third", new BigDecimal("300.00"), 1);
        List<Bill> orderedBills = Arrays.asList(bill1, bill2, bill3);

        when(repository.findAll()).thenReturn(orderedBills);

        // When
        List<Bill> result = getAllBills.execute();

        // Then: Order should be preserved
        assertThat(result)
                .hasSize(3)
                .containsExactly(bill1, bill2, bill3);
    }
}
