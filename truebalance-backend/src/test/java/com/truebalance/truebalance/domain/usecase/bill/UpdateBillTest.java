package com.truebalance.truebalance.domain.usecase.bill;

import com.truebalance.truebalance.domain.entity.Bill;
import com.truebalance.truebalance.domain.port.BillRepositoryPort;
import com.truebalance.truebalance.domain.usecase.UpdateBill;
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
import java.math.RoundingMode;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for UpdateBill use case.
 *
 * Tests update logic, existence validation, and installment recalculation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateBill Use Case Tests")
class UpdateBillTest {

    @Mock
    private BillRepositoryPort repository;

    @InjectMocks
    private UpdateBill useCase;

    @Captor
    private ArgumentCaptor<Bill> billCaptor;

    // ==================== Happy Path Tests ====================

    @Test
    @DisplayName("Should update existing bill successfully")
    void shouldUpdateExistingBillSuccessfully() {
        // Given: Existing bill
        Long billId = 1L;
        Bill existingBill = TestDataBuilder.createBill(billId, "Original Bill", new BigDecimal("1000.00"), 10);

        Bill updateData = TestDataBuilder.createBill(null, "Updated Bill", new BigDecimal("1500.00"), 5);
        Bill updatedBill = TestDataBuilder.createBill(billId, "Updated Bill", new BigDecimal("1500.00"), 5);
        updatedBill.setInstallmentAmount(new BigDecimal("300.00"));

        when(repository.findById(billId)).thenReturn(Optional.of(existingBill));
        when(repository.save(any(Bill.class))).thenReturn(updatedBill);

        // When
        Optional<Bill> result = useCase.updateBill(billId, updateData);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(billId);
        assertThat(result.get().getName()).isEqualTo("Updated Bill");
        assertThat(result.get().getTotalAmount()).isEqualByComparingTo("1500.00");
        assertThat(result.get().getNumberOfInstallments()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should return empty Optional when bill does not exist")
    void shouldReturnEmptyWhenBillDoesNotExist() {
        // Given: Non-existent bill
        Long billId = 999L;
        Bill updateData = TestDataBuilder.createBill(null, "Update Attempt", new BigDecimal("500.00"), 2);

        when(repository.findById(billId)).thenReturn(Optional.empty());

        // When
        Optional<Bill> result = useCase.updateBill(billId, updateData);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should preserve bill ID during update")
    void shouldPreserveBillIdDuringUpdate() {
        // Given: Update data without ID
        Long billId = 5L;
        Bill existingBill = TestDataBuilder.createBill(billId, "Original", new BigDecimal("1000.00"), 10);
        Bill updateData = TestDataBuilder.createBill(null, "Updated", new BigDecimal("2000.00"), 20);

        when(repository.findById(billId)).thenReturn(Optional.of(existingBill));
        when(repository.save(any(Bill.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        useCase.updateBill(billId, updateData);

        // Then: ID should be set to the path parameter
        verify(repository).save(billCaptor.capture());
        Bill capturedBill = billCaptor.getValue();

        assertThat(capturedBill.getId()).isEqualTo(billId);
    }

    // ==================== Installment Calculation Tests ====================

    @Test
    @DisplayName("Should recalculate installment amount for multiple installments")
    void shouldRecalculateInstallmentAmountForMultipleInstallments() {
        // Given: Update to multiple installments
        Long billId = 1L;
        Bill existingBill = TestDataBuilder.createBill(billId, "Original", new BigDecimal("1000.00"), 10);
        Bill updateData = TestDataBuilder.createBill(null, "Updated", new BigDecimal("1200.00"), 6);

        when(repository.findById(billId)).thenReturn(Optional.of(existingBill));
        when(repository.save(any(Bill.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        useCase.updateBill(billId, updateData);

        // Then: 1200 / 6 = 200.00
        verify(repository).save(billCaptor.capture());
        Bill capturedBill = billCaptor.getValue();

        assertThat(capturedBill.getInstallmentAmount()).isEqualByComparingTo("200.00");
    }

    @Test
    @DisplayName("Should set installment amount equal to total for single installment")
    void shouldSetInstallmentAmountEqualToTotalForSingleInstallment() {
        // Given: Update to single installment
        Long billId = 1L;
        Bill existingBill = TestDataBuilder.createBill(billId, "Original", new BigDecimal("1000.00"), 10);
        Bill updateData = TestDataBuilder.createBill(null, "Updated", new BigDecimal("750.00"), 1);

        when(repository.findById(billId)).thenReturn(Optional.of(existingBill));
        when(repository.save(any(Bill.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        useCase.updateBill(billId, updateData);

        // Then: Installment amount should equal total
        verify(repository).save(billCaptor.capture());
        Bill capturedBill = billCaptor.getValue();

        assertThat(capturedBill.getInstallmentAmount()).isEqualByComparingTo("750.00");
    }

    @Test
    @DisplayName("Should apply HALF_UP rounding when updating")
    void shouldApplyHalfUpRoundingWhenUpdating() {
        // Given: Update that requires rounding
        Long billId = 1L;
        Bill existingBill = TestDataBuilder.createBill(billId, "Original", new BigDecimal("1000.00"), 10);
        Bill updateData = TestDataBuilder.createBill(null, "Updated", new BigDecimal("1000.00"), 3);

        when(repository.findById(billId)).thenReturn(Optional.of(existingBill));
        when(repository.save(any(Bill.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        useCase.updateBill(billId, updateData);

        // Then: 1000 / 3 = 333.33 (HALF_UP)
        verify(repository).save(billCaptor.capture());
        Bill capturedBill = billCaptor.getValue();

        assertThat(capturedBill.getInstallmentAmount()).isEqualByComparingTo("333.33");
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should handle update from single to multiple installments")
    void shouldHandleUpdateFromSingleToMultipleInstallments() {
        // Given: Bill with 1 installment updating to multiple
        Long billId = 1L;
        Bill existingBill = TestDataBuilder.createBill(billId, "Single", new BigDecimal("1000.00"), 1);
        Bill updateData = TestDataBuilder.createBill(null, "Multiple", new BigDecimal("1000.00"), 10);

        when(repository.findById(billId)).thenReturn(Optional.of(existingBill));
        when(repository.save(any(Bill.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        useCase.updateBill(billId, updateData);

        // Then: Should divide correctly
        verify(repository).save(billCaptor.capture());
        Bill capturedBill = billCaptor.getValue();

        assertThat(capturedBill.getInstallmentAmount()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Should handle update from multiple to single installment")
    void shouldHandleUpdateFromMultipleToSingleInstallment() {
        // Given: Bill with multiple installments updating to single
        Long billId = 1L;
        Bill existingBill = TestDataBuilder.createBill(billId, "Multiple", new BigDecimal("1000.00"), 10);
        Bill updateData = TestDataBuilder.createBill(null, "Single", new BigDecimal("500.00"), 1);

        when(repository.findById(billId)).thenReturn(Optional.of(existingBill));
        when(repository.save(any(Bill.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        useCase.updateBill(billId, updateData);

        // Then: Installment should equal total
        verify(repository).save(billCaptor.capture());
        Bill capturedBill = billCaptor.getValue();

        assertThat(capturedBill.getInstallmentAmount()).isEqualByComparingTo("500.00");
    }

    @Test
    @DisplayName("Should handle very small amounts in update")
    void shouldHandleVerySmallAmountsInUpdate() {
        // Given: Update to small amount
        Long billId = 1L;
        Bill existingBill = TestDataBuilder.createBill(billId, "Original", new BigDecimal("1000.00"), 10);
        Bill updateData = TestDataBuilder.createBill(null, "Small", new BigDecimal("0.10"), 2);

        when(repository.findById(billId)).thenReturn(Optional.of(existingBill));
        when(repository.save(any(Bill.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        useCase.updateBill(billId, updateData);

        // Then: 0.10 / 2 = 0.05
        verify(repository).save(billCaptor.capture());
        Bill capturedBill = billCaptor.getValue();

        assertThat(capturedBill.getInstallmentAmount()).isEqualByComparingTo("0.05");
    }

    @Test
    @DisplayName("Should handle very large amounts in update")
    void shouldHandleVeryLargeAmountsInUpdate() {
        // Given: Update to large amount
        Long billId = 1L;
        Bill existingBill = TestDataBuilder.createBill(billId, "Original", new BigDecimal("1000.00"), 10);
        Bill updateData = TestDataBuilder.createBill(null, "Large", new BigDecimal("999999999.99"), 12);

        when(repository.findById(billId)).thenReturn(Optional.of(existingBill));
        when(repository.save(any(Bill.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        useCase.updateBill(billId, updateData);

        // Then: Should calculate correctly
        verify(repository).save(billCaptor.capture());
        Bill capturedBill = billCaptor.getValue();

        BigDecimal expected = new BigDecimal("999999999.99")
                .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        assertThat(capturedBill.getInstallmentAmount()).isEqualByComparingTo(expected);
    }

    @Test
    @DisplayName("Should preserve all bill fields during update")
    void shouldPreserveAllBillFieldsDuringUpdate() {
        // Given: Update with all fields
        Long billId = 1L;
        Bill existingBill = TestDataBuilder.createBill(billId, "Original", new BigDecimal("1000.00"), 10);

        Bill updateData = TestDataBuilder.createBill(null, "Updated Bill Name", new BigDecimal("1500.00"), 5);
        updateData.setDescription("New description");

        when(repository.findById(billId)).thenReturn(Optional.of(existingBill));
        when(repository.save(any(Bill.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Optional<Bill> result = useCase.updateBill(billId, updateData);

        // Then: All fields should be updated
        verify(repository).save(billCaptor.capture());
        Bill capturedBill = billCaptor.getValue();

        assertThat(capturedBill.getId()).isEqualTo(billId);
        assertThat(capturedBill.getName()).isEqualTo("Updated Bill Name");
        assertThat(capturedBill.getDescription()).isEqualTo("New description");
        assertThat(capturedBill.getTotalAmount()).isEqualByComparingTo("1500.00");
        assertThat(capturedBill.getNumberOfInstallments()).isEqualTo(5);
        assertThat(capturedBill.getInstallmentAmount()).isEqualByComparingTo("300.00");
    }

    @Test
    @DisplayName("Should maintain 2 decimal places precision in update")
    void shouldMaintainTwoDecimalPlacesPrecisionInUpdate() {
        // Given: Update with precision test
        Long billId = 1L;
        Bill existingBill = TestDataBuilder.createBill(billId, "Original", new BigDecimal("1000.00"), 10);
        Bill updateData = TestDataBuilder.createBill(null, "Updated", new BigDecimal("100.00"), 7);

        when(repository.findById(billId)).thenReturn(Optional.of(existingBill));
        when(repository.save(any(Bill.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        useCase.updateBill(billId, updateData);

        // Then: 100 / 7 = 14.29 (2 decimal places)
        verify(repository).save(billCaptor.capture());
        Bill capturedBill = billCaptor.getValue();

        assertThat(capturedBill.getInstallmentAmount()).isEqualByComparingTo("14.29");
        assertThat(capturedBill.getInstallmentAmount().scale()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should handle updating to same values")
    void shouldHandleUpdatingToSameValues() {
        // Given: Update with same values
        Long billId = 1L;
        Bill existingBill = TestDataBuilder.createBill(billId, "Test Bill", new BigDecimal("1000.00"), 10);
        Bill updateData = TestDataBuilder.createBill(null, "Test Bill", new BigDecimal("1000.00"), 10);

        when(repository.findById(billId)).thenReturn(Optional.of(existingBill));
        when(repository.save(any(Bill.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Optional<Bill> result = useCase.updateBill(billId, updateData);

        // Then: Should still process update
        assertThat(result).isPresent();
        verify(repository).save(billCaptor.capture());
        Bill capturedBill = billCaptor.getValue();

        assertThat(capturedBill.getTotalAmount()).isEqualByComparingTo("1000.00");
        assertThat(capturedBill.getNumberOfInstallments()).isEqualTo(10);
        assertThat(capturedBill.getInstallmentAmount()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Should update bill to recurring")
    void shouldUpdateBillToRecurring() {
        // Given: Non-recurring bill updating to recurring
        Long billId = 1L;
        Bill existingBill = TestDataBuilder.createBill(billId, "Internet Bill", new BigDecimal("100.00"), 1);
        existingBill.setIsRecurring(false);

        Bill updateData = TestDataBuilder.createBill(null, "Internet Bill", new BigDecimal("100.00"), 1);
        updateData.setIsRecurring(true);

        when(repository.findById(billId)).thenReturn(Optional.of(existingBill));
        when(repository.save(any(Bill.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Optional<Bill> result = useCase.updateBill(billId, updateData);

        // Then: Should update to recurring
        assertThat(result).isPresent();
        verify(repository).save(billCaptor.capture());
        Bill capturedBill = billCaptor.getValue();
        assertThat(capturedBill.getIsRecurring()).isTrue();
    }

    @Test
    @DisplayName("Should update bill from recurring to non-recurring")
    void shouldUpdateBillFromRecurringToNonRecurring() {
        // Given: Recurring bill updating to non-recurring
        Long billId = 1L;
        Bill existingBill = TestDataBuilder.createBill(billId, "Internet Bill", new BigDecimal("100.00"), 1);
        existingBill.setIsRecurring(true);

        Bill updateData = TestDataBuilder.createBill(null, "Internet Bill", new BigDecimal("100.00"), 1);
        updateData.setIsRecurring(false);

        when(repository.findById(billId)).thenReturn(Optional.of(existingBill));
        when(repository.save(any(Bill.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Optional<Bill> result = useCase.updateBill(billId, updateData);

        // Then: Should update to non-recurring
        assertThat(result).isPresent();
        verify(repository).save(billCaptor.capture());
        Bill capturedBill = billCaptor.getValue();
        assertThat(capturedBill.getIsRecurring()).isFalse();
    }

    @Test
    @DisplayName("Should preserve recurring flag when updating other fields")
    void shouldPreserveRecurringFlagWhenUpdatingOtherFields() {
        // Given: Recurring bill updating amount
        Long billId = 1L;
        Bill existingBill = TestDataBuilder.createBill(billId, "Internet Bill", new BigDecimal("100.00"), 1);
        existingBill.setIsRecurring(true);

        Bill updateData = TestDataBuilder.createBill(null, "Internet Bill", new BigDecimal("120.00"), 1);
        updateData.setIsRecurring(true);

        when(repository.findById(billId)).thenReturn(Optional.of(existingBill));
        when(repository.save(any(Bill.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Optional<Bill> result = useCase.updateBill(billId, updateData);

        // Then: Recurring flag should be preserved
        assertThat(result).isPresent();
        verify(repository).save(billCaptor.capture());
        Bill capturedBill = billCaptor.getValue();
        assertThat(capturedBill.getIsRecurring()).isTrue();
        assertThat(capturedBill.getTotalAmount()).isEqualByComparingTo("120.00");
    }
}
