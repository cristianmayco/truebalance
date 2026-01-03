package com.truebalance.truebalance.domain.service;

import com.truebalance.truebalance.domain.entity.InstallmentDateInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for InstallmentDateCalculator - a pure domain service with no dependencies.
 * This service calculates due dates and reference months for installments based on
 * credit card billing cycles.
 *
 * Business Rules Tested:
 * - BR-I-004: Billing cycle calculation based on credit card closing day
 */
@DisplayName("InstallmentDateCalculator - Domain Service Tests")
class InstallmentDateCalculatorTest {

    private InstallmentDateCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new InstallmentDateCalculator();
    }

    // ==================== HAPPY PATH (6 tests) ====================

    @Test
    @DisplayName("Should calculate correct due date for first installment")
    void shouldCalculateCorrectDueDateForFirstInstallment() {
        // Given: Purchase on January 15, card with closing day 10, due day 17
        // Purchase after closing (15 >= 10) → first installment vences next month
        LocalDateTime executionDate = LocalDateTime.of(2025, 1, 15, 10, 0);
        int closingDay = 10;
        int dueDay = 17;
        int installmentNumber = 1;

        // When
        InstallmentDateInfo result = calculator.calculate(executionDate, closingDay, dueDay, installmentNumber);

        // Then: Purchase after closing → first installment vences next month (17/02)
        assertThat(result.getDueDate()).isEqualTo(LocalDate.of(2025, 2, 17));
        assertThat(result.getInstallmentNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should calculate correct due date for second installment")
    void shouldCalculateCorrectDueDateForSecondInstallment() {
        // Given: Purchase on January 15, card with closing day 10, due day 17
        // Purchase after closing (15 >= 10) → first installment vences February, second vences March
        LocalDateTime executionDate = LocalDateTime.of(2025, 1, 15, 10, 0);
        int closingDay = 10;
        int dueDay = 17;
        int installmentNumber = 2;

        // When
        InstallmentDateInfo result = calculator.calculate(executionDate, closingDay, dueDay, installmentNumber);

        // Then: Second installment should be due on March 17 (first was February)
        assertThat(result.getDueDate()).isEqualTo(LocalDate.of(2025, 3, 17));
        assertThat(result.getInstallmentNumber()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should calculate correct reference month when purchase is after closing day")
    void shouldCalculateCorrectReferenceMonthWhenPurchaseIsAfterClosingDay() {
        // Given: Purchase on January 15, closing day 10, due day 17
        // Purchase date (15) is AFTER closing day (10) → next month's invoice
        LocalDateTime executionDate = LocalDateTime.of(2025, 1, 15, 10, 0);
        int closingDay = 10;
        int dueDay = 17;
        int installmentNumber = 1;

        // When
        InstallmentDateInfo result = calculator.calculate(executionDate, closingDay, dueDay, installmentNumber);

        // Then: Reference month should be NEXT month (February)
        assertThat(result.getReferenceMonth()).isEqualTo(LocalDate.of(2025, 2, 1));
    }

    @Test
    @DisplayName("Should calculate correct reference month when purchase on day 29 with closing day 21")
    void shouldCalculateCorrectReferenceMonthWhenPurchaseOnDay29WithClosingDay21() {
        // Given: Purchase on January 29, closing day 21
        // Purchase date (29) is AFTER closing day (21) → next month's invoice
        LocalDateTime executionDate = LocalDateTime.of(2025, 1, 29, 10, 0);
        int closingDay = 21;
        int dueDay = 17;
        int installmentNumber = 1;

        // When
        InstallmentDateInfo result = calculator.calculate(executionDate, closingDay, dueDay, installmentNumber);

        // Then: Reference month should be NEXT month (February)
        assertThat(result.getReferenceMonth()).isEqualTo(LocalDate.of(2025, 2, 1));
    }

    @Test
    @DisplayName("Should calculate correct reference month when purchase is before closing day")
    void shouldCalculateCorrectReferenceMonthWhenPurchaseIsBeforeClosingDay() {
        // Given: Purchase on January 15, closing day 20, due day 17
        // Purchase date (15) is BEFORE closing day (20) → current month's invoice
        LocalDateTime executionDate = LocalDateTime.of(2025, 1, 15, 10, 0);
        int closingDay = 20;
        int dueDay = 17;
        int installmentNumber = 1;

        // When
        InstallmentDateInfo result = calculator.calculate(executionDate, closingDay, dueDay, installmentNumber);

        // Then: Reference month should be CURRENT month (January)
        assertThat(result.getReferenceMonth()).isEqualTo(LocalDate.of(2025, 1, 1));
    }

    @Test
    @DisplayName("Should calculate correct reference month when due date equals closing day")
    void shouldCalculateCorrectReferenceMonthWhenDueDateEqualsClosingDay() {
        // Given: Purchase on January 15, closing day 17, due day 17
        // Due date (17) EQUALS closing day (17)
        LocalDateTime executionDate = LocalDateTime.of(2025, 1, 15, 10, 0);
        int closingDay = 17;
        int dueDay = 17;
        int installmentNumber = 1;

        // When
        InstallmentDateInfo result = calculator.calculate(executionDate, closingDay, dueDay, installmentNumber);

        // Then: Reference month should be CURRENT month (closing day is INCLUSIVE)
        assertThat(result.getReferenceMonth()).isEqualTo(LocalDate.of(2025, 1, 1));
    }

    @Test
    @DisplayName("Should calculate multiple installments correctly")
    void shouldCalculateMultipleInstallmentsCorrectly() {
        // Given: Purchase on January 15, closing day 10, 4 installments
        // Purchase after closing (15 >= 10) → first installment vences February
        LocalDateTime executionDate = LocalDateTime.of(2025, 1, 15, 10, 0);
        int closingDay = 10;
        int dueDay = 17;

        // When: Calculate all 4 installments
        InstallmentDateInfo inst1 = calculator.calculate(executionDate, closingDay, dueDay, 1);
        InstallmentDateInfo inst2 = calculator.calculate(executionDate, closingDay, dueDay, 2);
        InstallmentDateInfo inst3 = calculator.calculate(executionDate, closingDay, dueDay, 3);
        InstallmentDateInfo inst4 = calculator.calculate(executionDate, closingDay, dueDay, 4);

        // Then: Each installment should be one month apart, starting from February
        assertThat(inst1.getDueDate()).isEqualTo(LocalDate.of(2025, 2, 17));
        assertThat(inst2.getDueDate()).isEqualTo(LocalDate.of(2025, 3, 17));
        assertThat(inst3.getDueDate()).isEqualTo(LocalDate.of(2025, 4, 17));
        assertThat(inst4.getDueDate()).isEqualTo(LocalDate.of(2025, 5, 17));

        // Reference months should match the month when each installment is due
        // Each installment belongs to the invoice of the month it vences
        assertThat(inst1.getReferenceMonth()).isEqualTo(LocalDate.of(2025, 2, 1)); // Vences 17/02 → February invoice
        assertThat(inst2.getReferenceMonth()).isEqualTo(LocalDate.of(2025, 3, 1)); // Vences 17/03 → March invoice
        assertThat(inst3.getReferenceMonth()).isEqualTo(LocalDate.of(2025, 4, 1)); // Vences 17/04 → April invoice
        assertThat(inst4.getReferenceMonth()).isEqualTo(LocalDate.of(2025, 5, 1)); // Vences 17/05 → May invoice
    }

    // ==================== EDGE CASES - INVALID DATES (6 tests) ====================

    @Test
    @DisplayName("Should handle February 31 as last day of February - Non-leap year")
    void shouldHandleFebruary31AsLastDayOfFebruary_NonLeapYear() {
        // Given: Purchase on January 31, 2025 (non-leap year), due day 31
        LocalDateTime executionDate = LocalDateTime.of(2025, 1, 31, 10, 0);
        int closingDay = 10;
        int dueDay = 31;
        int installmentNumber = 2; // February

        // When
        InstallmentDateInfo result = calculator.calculate(executionDate, closingDay, dueDay, installmentNumber);

        // Then: Should fall back to February 28 (2025 is not a leap year)
        assertThat(result.getDueDate()).isEqualTo(LocalDate.of(2025, 2, 28));
    }

    @Test
    @DisplayName("Should handle February 31 as last day of February - Leap year")
    void shouldHandleFebruary31AsLastDayOfFebruary_LeapYear() {
        // Given: Purchase on January 31, 2024 (leap year), due day 31
        LocalDateTime executionDate = LocalDateTime.of(2024, 1, 31, 10, 0);
        int closingDay = 10;
        int dueDay = 31;
        int installmentNumber = 2; // February

        // When
        InstallmentDateInfo result = calculator.calculate(executionDate, closingDay, dueDay, installmentNumber);

        // Then: Should fall back to February 29 (2024 is a leap year)
        assertThat(result.getDueDate()).isEqualTo(LocalDate.of(2024, 2, 29));
    }

    @Test
    @DisplayName("Should handle February 30 as last day of February")
    void shouldHandleFebruary30AsLastDayOfFebruary() {
        // Given: Purchase on January 30, due day 30
        LocalDateTime executionDate = LocalDateTime.of(2025, 1, 30, 10, 0);
        int closingDay = 10;
        int dueDay = 30;
        int installmentNumber = 2; // February

        // When
        InstallmentDateInfo result = calculator.calculate(executionDate, closingDay, dueDay, installmentNumber);

        // Then: Should fall back to February 28
        assertThat(result.getDueDate()).isEqualTo(LocalDate.of(2025, 2, 28));
    }

    @Test
    @DisplayName("Should handle April 31 as April 30")
    void shouldHandleApril31AsApril30() {
        // Given: Purchase on March 31, due day 31
        LocalDateTime executionDate = LocalDateTime.of(2025, 3, 31, 10, 0);
        int closingDay = 10;
        int dueDay = 31;
        int installmentNumber = 2; // April (30 days)

        // When
        InstallmentDateInfo result = calculator.calculate(executionDate, closingDay, dueDay, installmentNumber);

        // Then: Should fall back to April 30
        assertThat(result.getDueDate()).isEqualTo(LocalDate.of(2025, 4, 30));
    }

    @Test
    @DisplayName("Should handle due day 31 in February across multiple months")
    void shouldHandleDueDay31InFebruaryAcrossMonths() {
        // Given: Purchase on January 31, due day 31, 3 installments
        LocalDateTime executionDate = LocalDateTime.of(2025, 1, 31, 10, 0);
        int closingDay = 10;
        int dueDay = 31;

        // When: Calculate 3 installments
        InstallmentDateInfo inst1 = calculator.calculate(executionDate, closingDay, dueDay, 1);
        InstallmentDateInfo inst2 = calculator.calculate(executionDate, closingDay, dueDay, 2);
        InstallmentDateInfo inst3 = calculator.calculate(executionDate, closingDay, dueDay, 3);

        // Then: January 31, February 28, March 31
        assertThat(inst1.getDueDate()).isEqualTo(LocalDate.of(2025, 1, 31));
        assertThat(inst2.getDueDate()).isEqualTo(LocalDate.of(2025, 2, 28));
        assertThat(inst3.getDueDate()).isEqualTo(LocalDate.of(2025, 3, 31));
    }

    @Test
    @DisplayName("Should handle due day at end of month")
    void shouldHandleDueDayAtEndOfMonth() {
        // Given: Purchase on January 15, closing day 10, due day 30
        // Purchase after closing (15 >= 10) → first installment vences February
        LocalDateTime executionDate = LocalDateTime.of(2025, 1, 15, 10, 0);
        int closingDay = 10;
        int dueDay = 30;

        // When: Calculate for first and second installments
        InstallmentDateInfo inst1 = calculator.calculate(executionDate, closingDay, dueDay, 1);
        InstallmentDateInfo inst2 = calculator.calculate(executionDate, closingDay, dueDay, 2);

        // Then: February 28 (fallback, since Feb has 28 days), March 30 (valid)
        assertThat(inst1.getDueDate()).isEqualTo(LocalDate.of(2025, 2, 28));
        assertThat(inst2.getDueDate()).isEqualTo(LocalDate.of(2025, 3, 30));
    }

    // ==================== BOUNDARY CONDITIONS (6 tests) ====================

    @Test
    @DisplayName("Should handle closing day 1 and due day 2")
    void shouldHandleClosingDay1DueDay2() {
        // Given: Purchase on January 15, closing day 1, due day 2
        // Purchase after closing (15 >= 1) → first installment vences next month
        LocalDateTime executionDate = LocalDateTime.of(2025, 1, 15, 10, 0);
        int closingDay = 1;
        int dueDay = 2;
        int installmentNumber = 1;

        // When
        InstallmentDateInfo result = calculator.calculate(executionDate, closingDay, dueDay, installmentNumber);

        // Then: First installment vences February 2, invoice is February
        assertThat(result.getDueDate()).isEqualTo(LocalDate.of(2025, 2, 2));
        assertThat(result.getReferenceMonth()).isEqualTo(LocalDate.of(2025, 2, 1));
    }

    @Test
    @DisplayName("Should handle closing day 31 and due day 1 - Month wrap")
    void shouldHandleClosingDay31DueDay1_MonthWrap() {
        // Given: Purchase on January 15, closing day 31, due day 1
        // Due date (1) is BEFORE closing day (31)
        LocalDateTime executionDate = LocalDateTime.of(2025, 1, 15, 10, 0);
        int closingDay = 31;
        int dueDay = 1;
        int installmentNumber = 1;

        // When
        InstallmentDateInfo result = calculator.calculate(executionDate, closingDay, dueDay, installmentNumber);

        // Then: Reference month should be current month
        assertThat(result.getDueDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(result.getReferenceMonth()).isEqualTo(LocalDate.of(2025, 1, 1));
    }

    @Test
    @DisplayName("Should handle closing day 15 and due day 20")
    void shouldHandleClosingDay15DueDay20() {
        // Given: Purchase on January 10, closing day 15, due day 20
        // Purchase date (10) < closing day (15) → current month's invoice
        LocalDateTime executionDate = LocalDateTime.of(2025, 1, 10, 10, 0);
        int closingDay = 15;
        int dueDay = 20;
        int installmentNumber = 1;

        // When
        InstallmentDateInfo result = calculator.calculate(executionDate, closingDay, dueDay, installmentNumber);

        // Then: Purchase before closing day → current month's invoice (January)
        assertThat(result.getDueDate()).isEqualTo(LocalDate.of(2025, 1, 20));
        assertThat(result.getReferenceMonth()).isEqualTo(LocalDate.of(2025, 1, 1));
    }

    @Test
    @DisplayName("Should calculate first installment when executed on closing day")
    void shouldCalculateFirstInstallmentWhenExecutedOnClosingDay() {
        // Given: Purchase EXACTLY on closing day (January 10)
        // Purchase on closing day (10 >= 10) → first installment vences next month
        LocalDateTime executionDate = LocalDateTime.of(2025, 1, 10, 10, 0);
        int closingDay = 10;
        int dueDay = 17;
        int installmentNumber = 1;

        // When
        InstallmentDateInfo result = calculator.calculate(executionDate, closingDay, dueDay, installmentNumber);

        // Then: First installment vences February 17, invoice is February
        assertThat(result.getDueDate()).isEqualTo(LocalDate.of(2025, 2, 17));
        assertThat(result.getReferenceMonth()).isEqualTo(LocalDate.of(2025, 2, 1));
    }

    @Test
    @DisplayName("Should calculate first installment when executed day before closing day")
    void shouldCalculateFirstInstallmentWhenExecutedDayBeforeClosingDay() {
        // Given: Purchase on day before closing day (January 9)
        // Purchase date (9) < closing day (10) → current month's invoice
        LocalDateTime executionDate = LocalDateTime.of(2025, 1, 9, 23, 59);
        int closingDay = 10;
        int dueDay = 17;
        int installmentNumber = 1;

        // When
        InstallmentDateInfo result = calculator.calculate(executionDate, closingDay, dueDay, installmentNumber);

        // Then: Purchase before closing day → current month's invoice (January)
        assertThat(result.getDueDate()).isEqualTo(LocalDate.of(2025, 1, 17));
        assertThat(result.getReferenceMonth()).isEqualTo(LocalDate.of(2025, 1, 1));
    }

    @Test
    @DisplayName("Should calculate first installment when executed day after closing day")
    void shouldCalculateFirstInstallmentWhenExecutedDayAfterClosingDay() {
        // Given: Purchase on day after closing day (January 11)
        // Purchase after closing (11 >= 10) → first installment vences next month
        LocalDateTime executionDate = LocalDateTime.of(2025, 1, 11, 0, 1);
        int closingDay = 10;
        int dueDay = 17;
        int installmentNumber = 1;

        // When
        InstallmentDateInfo result = calculator.calculate(executionDate, closingDay, dueDay, installmentNumber);

        // Then: Purchase after closing → first installment vences next month (17/02)
        //       Invoice is next month (February)
        assertThat(result.getDueDate()).isEqualTo(LocalDate.of(2025, 2, 17));
        assertThat(result.getReferenceMonth()).isEqualTo(LocalDate.of(2025, 2, 1));
    }

    @Test
    @DisplayName("Should assign purchase on day 29 to next month when closing day is 21")
    void shouldAssignPurchaseOnDay29ToNextMonthWhenClosingDayIs21() {
        // Given: Purchase on December 29, closing day 21, due day 28
        // This is the specific case reported by the user
        LocalDateTime executionDate = LocalDateTime.of(2025, 12, 29, 10, 0);
        int closingDay = 21;
        int dueDay = 28;
        int installmentNumber = 1;

        // When
        InstallmentDateInfo result = calculator.calculate(executionDate, closingDay, dueDay, installmentNumber);

        // Then: Purchase on day 29 (after closing day 21) → next month's invoice (January)
        //       First installment should vence in January (28/01), not December (28/12)
        assertThat(result.getDueDate()).isEqualTo(LocalDate.of(2026, 1, 28));
        assertThat(result.getReferenceMonth()).isEqualTo(LocalDate.of(2026, 1, 1));
    }

    @Test
    @DisplayName("Should assign purchase on day 20 to current month when closing day is 21")
    void shouldAssignPurchaseOnDay20ToCurrentMonthWhenClosingDayIs21() {
        // Given: Purchase on January 20, closing day 21, due day 28
        // Purchase before closing day → current month's invoice
        // Due day (28) hasn't passed (20 < 28) → first installment vences current month
        LocalDateTime executionDate = LocalDateTime.of(2025, 1, 20, 10, 0);
        int closingDay = 21;
        int dueDay = 28;
        int installmentNumber = 1;

        // When
        InstallmentDateInfo result = calculator.calculate(executionDate, closingDay, dueDay, installmentNumber);

        // Then: Purchase on day 20 (before closing day 21) → current month's invoice (January)
        //       First installment vences January 28
        assertThat(result.getDueDate()).isEqualTo(LocalDate.of(2025, 1, 28));
        assertThat(result.getReferenceMonth()).isEqualTo(LocalDate.of(2025, 1, 1));
    }

    @Test
    @DisplayName("Should assign purchase on closing day 21 to next month")
    void shouldAssignPurchaseOnClosingDay21ToNextMonth() {
        // Given: Purchase on January 21 (closing day), closing day 21, due day 28
        // Purchase on closing day (21 >= 21) → first installment vences next month
        LocalDateTime executionDate = LocalDateTime.of(2025, 1, 21, 10, 0);
        int closingDay = 21;
        int dueDay = 28;
        int installmentNumber = 1;

        // When
        InstallmentDateInfo result = calculator.calculate(executionDate, closingDay, dueDay, installmentNumber);

        // Then: Purchase on closing day 21 → next month's invoice (February)
        //       First installment vences February 28
        assertThat(result.getDueDate()).isEqualTo(LocalDate.of(2025, 2, 28));
        assertThat(result.getReferenceMonth()).isEqualTo(LocalDate.of(2025, 2, 1));
    }

    @Test
    @DisplayName("Should assign second installment to correct month when due date equals closing day")
    void shouldAssignSecondInstallmentToCorrectMonthWhenDueDateEqualsClosingDay() {
        // Given: Purchase on December 12, closing day 10, due day 10, 2 installments
        // Purchase after closing (12 >= 10) → first installment vences January
        // Second installment vences February 10, which equals closing day 10
        LocalDateTime executionDate = LocalDateTime.of(2025, 12, 12, 10, 0);
        int closingDay = 10;
        int dueDay = 10;

        // When: Calculate both installments
        InstallmentDateInfo inst1 = calculator.calculate(executionDate, closingDay, dueDay, 1);
        InstallmentDateInfo inst2 = calculator.calculate(executionDate, closingDay, dueDay, 2);

        // Then: First installment should be in January, second should be in February (not March)
        assertThat(inst1.getDueDate()).isEqualTo(LocalDate.of(2026, 1, 10));
        assertThat(inst1.getReferenceMonth()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(inst2.getDueDate()).isEqualTo(LocalDate.of(2026, 2, 10));
        assertThat(inst2.getReferenceMonth()).isEqualTo(LocalDate.of(2026, 2, 1)); // Should be February, not March
    }
}
