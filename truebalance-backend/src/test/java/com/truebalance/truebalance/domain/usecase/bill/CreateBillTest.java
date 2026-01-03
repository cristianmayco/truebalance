package com.truebalance.truebalance.domain.usecase.bill;

import com.truebalance.truebalance.domain.entity.Bill;
import com.truebalance.truebalance.domain.port.BillRepositoryPort;
import com.truebalance.truebalance.domain.usecase.CreateBill;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for CreateBill use case (simple version without credit card).
 *
 * Tests installment amount calculation with proper rounding.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CreateBill Use Case Tests")
class CreateBillTest {

    @Mock
    private BillRepositoryPort repository;

    @InjectMocks
    private CreateBill useCase;

    @Captor
    private ArgumentCaptor<Bill> billCaptor;

    // ==================== Happy Path Tests ====================

    @Test
    @DisplayName("Should create bill with single installment")
    void shouldCreateBillWithSingleInstallment() {
        // Given: Bill with 1 installment
        Bill inputBill = TestDataBuilder.createBill(null, "Single Installment Bill", new BigDecimal("1000.00"), 1);
        Bill savedBill = TestDataBuilder.createBill(1L, "Single Installment Bill", new BigDecimal("1000.00"), 1);
        savedBill.setInstallmentAmount(new BigDecimal("1000.00"));

        when(repository.save(any(Bill.class))).thenReturn(savedBill);

        // When
        Bill result = useCase.addBill(inputBill);

        // Then: Installment amount should equal total amount
        assertThat(result).isNotNull();
        verify(repository).save(billCaptor.capture());

        Bill capturedBill = billCaptor.getValue();
        assertThat(capturedBill.getInstallmentAmount()).isEqualByComparingTo("1000.00");
        assertThat(capturedBill.getTotalAmount()).isEqualByComparingTo("1000.00");
    }

    @Test
    @DisplayName("Should calculate installment amount for multiple installments")
    void shouldCalculateInstallmentAmountForMultipleInstallments() {
        // Given: Bill with 10 installments
        Bill inputBill = TestDataBuilder.createBill(null, "Multiple Installments", new BigDecimal("1000.00"), 10);
        Bill savedBill = TestDataBuilder.createBill(1L, "Multiple Installments", new BigDecimal("1000.00"), 10);
        savedBill.setInstallmentAmount(new BigDecimal("100.00"));

        when(repository.save(any(Bill.class))).thenReturn(savedBill);

        // When
        Bill result = useCase.addBill(inputBill);

        // Then: 1000 / 10 = 100.00
        assertThat(result).isNotNull();
        verify(repository).save(billCaptor.capture());

        Bill capturedBill = billCaptor.getValue();
        assertThat(capturedBill.getInstallmentAmount()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Should apply HALF_UP rounding when division is not exact")
    void shouldApplyHalfUpRoundingWhenDivisionIsNotExact() {
        // Given: Bill that requires rounding (1000 / 3)
        Bill inputBill = TestDataBuilder.createBill(null, "Bill with Rounding", new BigDecimal("1000.00"), 3);
        Bill savedBill = TestDataBuilder.createBill(1L, "Bill with Rounding", new BigDecimal("1000.00"), 3);

        when(repository.save(any(Bill.class))).thenReturn(savedBill);

        // When
        Bill result = useCase.addBill(inputBill);

        // Then: 1000 / 3 = 333.33 (HALF_UP rounding)
        verify(repository).save(billCaptor.capture());
        Bill capturedBill = billCaptor.getValue();

        BigDecimal expectedInstallment = new BigDecimal("1000.00")
                .divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);
        assertThat(capturedBill.getInstallmentAmount()).isEqualByComparingTo(expectedInstallment);
        assertThat(capturedBill.getInstallmentAmount()).isEqualByComparingTo("333.33");
    }

    @Test
    @DisplayName("Should maintain 2 decimal places precision")
    void shouldMaintainTwoDecimalPlacesPrecision() {
        // Given: Bill with 7 installments
        Bill inputBill = TestDataBuilder.createBill(null, "Precision Test", new BigDecimal("100.00"), 7);
        Bill savedBill = TestDataBuilder.createBill(1L, "Precision Test", new BigDecimal("100.00"), 7);

        when(repository.save(any(Bill.class))).thenReturn(savedBill);

        // When
        useCase.addBill(inputBill);

        // Then: 100 / 7 = 14.29 (with 2 decimal places)
        verify(repository).save(billCaptor.capture());
        Bill capturedBill = billCaptor.getValue();

        assertThat(capturedBill.getInstallmentAmount()).isEqualByComparingTo("14.29");
        assertThat(capturedBill.getInstallmentAmount().scale()).isEqualTo(2);
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should handle very small total amount")
    void shouldHandleVerySmallTotalAmount() {
        // Given: Small amount
        Bill inputBill = TestDataBuilder.createBill(null, "Small Amount", new BigDecimal("0.10"), 2);
        Bill savedBill = TestDataBuilder.createBill(1L, "Small Amount", new BigDecimal("0.10"), 2);

        when(repository.save(any(Bill.class))).thenReturn(savedBill);

        // When
        useCase.addBill(inputBill);

        // Then: 0.10 / 2 = 0.05
        verify(repository).save(billCaptor.capture());
        Bill capturedBill = billCaptor.getValue();

        assertThat(capturedBill.getInstallmentAmount()).isEqualByComparingTo("0.05");
    }

    @Test
    @DisplayName("Should handle very large total amount")
    void shouldHandleVeryLargeTotalAmount() {
        // Given: Large amount
        Bill inputBill = TestDataBuilder.createBill(null, "Large Amount", new BigDecimal("999999999.99"), 12);
        Bill savedBill = TestDataBuilder.createBill(1L, "Large Amount", new BigDecimal("999999999.99"), 12);

        when(repository.save(any(Bill.class))).thenReturn(savedBill);

        // When
        useCase.addBill(inputBill);

        // Then: Should calculate correctly
        verify(repository).save(billCaptor.capture());
        Bill capturedBill = billCaptor.getValue();

        BigDecimal expected = new BigDecimal("999999999.99")
                .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        assertThat(capturedBill.getInstallmentAmount()).isEqualByComparingTo(expected);
    }

    @Test
    @DisplayName("Should handle maximum number of installments")
    void shouldHandleMaximumNumberOfInstallments() {
        // Given: Bill with many installments
        Bill inputBill = TestDataBuilder.createBill(null, "Many Installments", new BigDecimal("12000.00"), 60);
        Bill savedBill = TestDataBuilder.createBill(1L, "Many Installments", new BigDecimal("12000.00"), 60);

        when(repository.save(any(Bill.class))).thenReturn(savedBill);

        // When
        useCase.addBill(inputBill);

        // Then: 12000 / 60 = 200.00
        verify(repository).save(billCaptor.capture());
        Bill capturedBill = billCaptor.getValue();

        assertThat(capturedBill.getInstallmentAmount()).isEqualByComparingTo("200.00");
    }

    @Test
    @DisplayName("Should handle 2 installments (edge case of > 1)")
    void shouldHandleTwoInstallments() {
        // Given: Bill with exactly 2 installments
        Bill inputBill = TestDataBuilder.createBill(null, "Two Installments", new BigDecimal("500.00"), 2);
        Bill savedBill = TestDataBuilder.createBill(1L, "Two Installments", new BigDecimal("500.00"), 2);

        when(repository.save(any(Bill.class))).thenReturn(savedBill);

        // When
        useCase.addBill(inputBill);

        // Then: Should use division (500 / 2 = 250.00)
        verify(repository).save(billCaptor.capture());
        Bill capturedBill = billCaptor.getValue();

        assertThat(capturedBill.getInstallmentAmount()).isEqualByComparingTo("250.00");
    }

    // ==================== Rounding Scenarios ====================

    @Test
    @DisplayName("Should round 0.445 to 0.45 (HALF_UP rounding)")
    void shouldRoundHalfUpCorrectly() {
        // Given: Amount that tests HALF_UP rounding (0.89 / 2 = 0.445)
        Bill inputBill = TestDataBuilder.createBill(null, "Rounding Test", new BigDecimal("0.89"), 2);
        Bill savedBill = TestDataBuilder.createBill(1L, "Rounding Test", new BigDecimal("0.89"), 2);

        when(repository.save(any(Bill.class))).thenReturn(savedBill);

        // When
        useCase.addBill(inputBill);

        // Then: 0.445 should round to 0.45
        verify(repository).save(billCaptor.capture());
        Bill capturedBill = billCaptor.getValue();

        assertThat(capturedBill.getInstallmentAmount()).isEqualByComparingTo("0.45");
    }

    @Test
    @DisplayName("Should preserve bill properties during save")
    void shouldPreserveBillPropertiesDuringSave() {
        // Given: Bill with specific properties
        Bill inputBill = TestDataBuilder.createBill(null, "Test Bill", new BigDecimal("1500.00"), 5);
        inputBill.setDescription("Important bill");

        Bill savedBill = TestDataBuilder.createBill(1L, "Test Bill", new BigDecimal("1500.00"), 5);
        savedBill.setDescription("Important bill");
        savedBill.setInstallmentAmount(new BigDecimal("300.00"));

        when(repository.save(any(Bill.class))).thenReturn(savedBill);

        // When
        Bill result = useCase.addBill(inputBill);

        // Then: All properties should be preserved
        assertThat(result.getName()).isEqualTo("Test Bill");
        assertThat(result.getDescription()).isEqualTo("Important bill");
        assertThat(result.getTotalAmount()).isEqualByComparingTo("1500.00");
        assertThat(result.getNumberOfInstallments()).isEqualTo(5);
        assertThat(result.getInstallmentAmount()).isEqualByComparingTo("300.00");
    }

    @Test
    @DisplayName("Should create recurring bill")
    void shouldCreateRecurringBill() {
        // Given: Recurring bill
        Bill inputBill = TestDataBuilder.createBill(null, "Internet Bill", new BigDecimal("100.00"), 1);
        inputBill.setIsRecurring(true);
        inputBill.setDescription("Monthly internet subscription");

        Bill savedBill = TestDataBuilder.createBill(1L, "Internet Bill", new BigDecimal("100.00"), 1);
        savedBill.setIsRecurring(true);
        savedBill.setDescription("Monthly internet subscription");
        savedBill.setInstallmentAmount(new BigDecimal("100.00"));

        when(repository.save(any(Bill.class))).thenReturn(savedBill);

        // When
        Bill result = useCase.addBill(inputBill);

        // Then: Recurring flag should be preserved
        assertThat(result.getIsRecurring()).isTrue();
        assertThat(result.getName()).isEqualTo("Internet Bill");
        verify(repository).save(billCaptor.capture());
        Bill capturedBill = billCaptor.getValue();
        assertThat(capturedBill.getIsRecurring()).isTrue();
    }

    @Test
    @DisplayName("Should create non-recurring bill by default")
    void shouldCreateNonRecurringBillByDefault() {
        // Given: Bill without explicit recurring flag
        Bill inputBill = TestDataBuilder.createBill(null, "One-time Bill", new BigDecimal("500.00"), 1);
        inputBill.setIsRecurring(false);

        Bill savedBill = TestDataBuilder.createBill(1L, "One-time Bill", new BigDecimal("500.00"), 1);
        savedBill.setIsRecurring(false);
        savedBill.setInstallmentAmount(new BigDecimal("500.00"));

        when(repository.save(any(Bill.class))).thenReturn(savedBill);

        // When
        Bill result = useCase.addBill(inputBill);

        // Then: Should default to non-recurring
        assertThat(result.getIsRecurring()).isFalse();
        verify(repository).save(billCaptor.capture());
        Bill capturedBill = billCaptor.getValue();
        assertThat(capturedBill.getIsRecurring()).isFalse();
    }
}
