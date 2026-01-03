package com.truebalance.truebalance.domain.usecase.bill;

import com.truebalance.truebalance.domain.entity.Bill;
import com.truebalance.truebalance.domain.port.BillRepositoryPort;
import com.truebalance.truebalance.domain.usecase.GetBillById;
import com.truebalance.truebalance.util.TestDataBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Tests for GetBillById use case.
 * Simple CRUD operation that retrieves a bill by its ID.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetBillById - Use Case Tests")
class GetBillByIdTest {

    @Mock
    private BillRepositoryPort repository;

    @InjectMocks
    private GetBillById getBillById;

    @Test
    @DisplayName("Should return bill when exists")
    void shouldReturnBillWhenExists() {
        // Given: Bill exists in repository
        Long billId = 1L;
        Bill bill = TestDataBuilder.createBill(billId, "Existing Bill", new BigDecimal("500.00"), 5);

        when(repository.findById(billId)).thenReturn(Optional.of(bill));

        // When
        Optional<Bill> result = getBillById.execute(billId);

        // Then
        assertThat(result)
                .isPresent()
                .hasValueSatisfying(b -> {
                    assertThat(b.getId()).isEqualTo(billId);
                    assertThat(b.getName()).isEqualTo("Existing Bill");
                    assertThat(b.getTotalAmount()).isEqualByComparingTo("500.00");
                    assertThat(b.getNumberOfInstallments()).isEqualTo(5);
                });
    }

    @Test
    @DisplayName("Should return empty optional when not found")
    void shouldReturnEmptyOptionalWhenNotFound() {
        // Given: Bill does not exist
        Long nonExistentId = 999L;

        when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        Optional<Bill> result = getBillById.execute(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle null ID")
    void shouldHandleNullId() {
        // Given: Null ID
        when(repository.findById(null)).thenReturn(Optional.empty());

        // When
        Optional<Bill> result = getBillById.execute(null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should call repository with correct ID")
    void shouldCallRepositoryWithCorrectId() {
        // Given
        Long billId = 42L;
        when(repository.findById(billId)).thenReturn(Optional.empty());

        // When
        getBillById.execute(billId);

        // Then
        verify(repository, times(1)).findById(billId);
        verify(repository, times(1)).findById(any(Long.class));
    }

    @Test
    @DisplayName("Should map bill correctly")
    void shouldMapBillCorrectly() {
        // Given: Bill with all fields populated
        Long billId = 123L;
        Bill bill = TestDataBuilder.createBill();
        bill.setId(billId);
        bill.setName("Complete Bill");
        bill.setTotalAmount(new BigDecimal("1234.56"));
        bill.setNumberOfInstallments(12);
        bill.setInstallmentAmount(new BigDecimal("102.88"));
        bill.setDescription("Full description");

        when(repository.findById(billId)).thenReturn(Optional.of(bill));

        // When
        Optional<Bill> result = getBillById.execute(billId);

        // Then: All fields should be present
        assertThat(result).isPresent();
        Bill returnedBill = result.get();
        assertThat(returnedBill.getId()).isEqualTo(billId);
        assertThat(returnedBill.getName()).isEqualTo("Complete Bill");
        assertThat(returnedBill.getTotalAmount()).isEqualByComparingTo("1234.56");
        assertThat(returnedBill.getNumberOfInstallments()).isEqualTo(12);
        assertThat(returnedBill.getInstallmentAmount()).isEqualByComparingTo("102.88");
        assertThat(returnedBill.getDescription()).isEqualTo("Full description");
    }

    @Test
    @DisplayName("Should handle repository exception")
    void shouldHandleRepositoryException() {
        // Given: Repository throws exception
        Long billId = 1L;
        when(repository.findById(billId)).thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThatThrownBy(() -> getBillById.execute(billId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database connection failed");
    }
}
