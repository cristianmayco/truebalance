package com.truebalance.truebalance.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Constants used across test classes to ensure consistency.
 */
public class TestConstants {

    // Test IDs
    public static final Long TEST_BILL_ID = 1L;
    public static final Long TEST_CREDIT_CARD_ID = 1L;
    public static final Long TEST_INVOICE_ID = 1L;
    public static final Long TEST_INSTALLMENT_ID = 1L;
    public static final Long TEST_PARTIAL_PAYMENT_ID = 1L;

    // Default amounts
    public static final BigDecimal DEFAULT_TOTAL_AMOUNT = new BigDecimal("1000.00");
    public static final BigDecimal DEFAULT_INSTALLMENT_AMOUNT = new BigDecimal("100.00");
    public static final BigDecimal DEFAULT_CREDIT_LIMIT = new BigDecimal("5000.00");
    public static final BigDecimal DEFAULT_INVOICE_AMOUNT = new BigDecimal("500.00");
    public static final BigDecimal DEFAULT_PARTIAL_PAYMENT_AMOUNT = new BigDecimal("200.00");
    public static final BigDecimal ZERO_AMOUNT = BigDecimal.ZERO;

    // Default installment counts
    public static final int DEFAULT_NUMBER_OF_INSTALLMENTS = 10;
    public static final int SINGLE_INSTALLMENT = 1;

    // Default dates
    public static final LocalDateTime DEFAULT_EXECUTION_DATE = LocalDateTime.of(2025, 1, 15, 10, 0);
    public static final LocalDate DEFAULT_DUE_DATE = LocalDate.of(2025, 1, 17);
    public static final LocalDate DEFAULT_REFERENCE_MONTH = LocalDate.of(2025, 1, 1);
    public static final LocalDateTime DEFAULT_CREATED_AT = LocalDateTime.of(2025, 1, 1, 10, 0);
    public static final LocalDateTime DEFAULT_PAYMENT_DATE = LocalDateTime.of(2025, 1, 15, 14, 30);

    // Credit card cycle defaults
    public static final int DEFAULT_CLOSING_DAY = 10;
    public static final int DEFAULT_DUE_DAY = 17;

    // Test names and descriptions
    public static final String TEST_BILL_NAME = "Test Bill";
    public static final String TEST_CREDIT_CARD_NAME = "Test Credit Card";
    public static final String TEST_DESCRIPTION = "Test description";

    // Edge case values
    public static final int MIN_DAY = 1;
    public static final int MAX_DAY = 31;
    public static final int FEBRUARY_29 = 29;
    public static final int FEBRUARY_30 = 30;
    public static final int FEBRUARY_31 = 31;
    public static final int APRIL_31 = 31; // Invalid day for April

    private TestConstants() {
        // Prevent instantiation
    }
}
