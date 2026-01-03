package com.truebalance.truebalance.domain.service;

import com.truebalance.truebalance.domain.entity.InstallmentDateInfo;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Domain service for calculating installment dates and their corresponding invoice reference months.
 *
 * This is a pure function service with no dependencies, handling complex billing cycle logic
 * including edge cases like invalid dates (e.g., February 31st).
 *
 * Business Rules:
 * - BR-I-004: Billing cycle calculation based on credit card closing day
 * - The invoice is determined by the purchase date (executionDate), not the due date
 * - Billing cycle: from closing day to the day before closing day of next month
 * - If purchase is ON or AFTER closing day, it goes to NEXT month's invoice
 * - If purchase is BEFORE closing day, it goes to CURRENT month's invoice
 * - Example: If closing day is 21, purchases from 21/12 to 20/01 belong to January invoice
 * - Invalid dates (e.g., dueDay=31 in February) fall back to last day of month
 */
public class InstallmentDateCalculator {

    /**
     * Calculate the due date and reference month for a specific installment.
     *
     * @param executionDate       when the bill was executed (purchase date)
     * @param closingDay          credit card closing day (1-31)
     * @param dueDay              credit card due day (1-31)
     * @param installmentNumber   which installment number (1-based)
     * @return InstallmentDateInfo containing calculated dates
     */
    public InstallmentDateInfo calculate(
            LocalDateTime executionDate,
            int closingDay,
            int dueDay,
            int installmentNumber) {

        LocalDate purchaseDate = executionDate.toLocalDate();
        
        // 1. Determine reference month (which invoice this installment belongs to)
        // RULE: The invoice is determined by the purchase date (executionDate) for the billing cycle
        //       Billing cycle: from closing day to the day before closing day of next month
        //       Example: If closing day is 21, purchases from 21/12 to 20/01 belong to January invoice
        //       If purchase is ON or AFTER closing day, it goes to NEXT month's invoice
        //       If purchase is BEFORE closing day, it goes to CURRENT month's invoice
        LocalDate referenceMonth;
        boolean purchaseAfterClosing = purchaseDate.getDayOfMonth() >= closingDay;
        
        if (purchaseAfterClosing) {
            // Purchase on or after closing day → next month's invoice
            // Example: Purchase on 21/12 → January invoice (closes 21/01)
            referenceMonth = purchaseDate.plusMonths(1).withDayOfMonth(1);
        } else {
            // Purchase before closing day → current month's invoice
            // Example: Purchase on 20/12 → December invoice (closes 21/12)
            referenceMonth = purchaseDate.withDayOfMonth(1);
        }

        // 2. Calculate due date
        // If purchase was after closing day, first installment vences in the next month
        // If purchase was before closing day, first installment vences in current month (if due day hasn't passed) or next month
        LocalDate baseDueDate;
        if (installmentNumber == 1) {
            // First installment
            if (purchaseAfterClosing) {
                // Purchase after closing → first installment vences next month
                // Example: Purchase 28/12, closing 21 → first installment vences 28/01
                baseDueDate = purchaseDate.plusMonths(1);
            } else {
                // Purchase before closing → check if due day already passed in current month
                if (dueDay < purchaseDate.getDayOfMonth()) {
                    // Due day already passed in current month → use next month
                    baseDueDate = purchaseDate.plusMonths(1);
                } else {
                    // Due day hasn't passed → use current month
                    baseDueDate = purchaseDate;
                }
            }
        } else {
            // Subsequent installments: calculate from first installment month
            // First, determine when first installment would be due
            LocalDate firstInstallmentMonth;
            if (purchaseAfterClosing) {
                firstInstallmentMonth = purchaseDate.plusMonths(1);
            } else {
                if (dueDay < purchaseDate.getDayOfMonth()) {
                    firstInstallmentMonth = purchaseDate.plusMonths(1);
                } else {
                    firstInstallmentMonth = purchaseDate;
                }
            }
            // Then add (installmentNumber - 1) months for subsequent installments
            baseDueDate = firstInstallmentMonth.plusMonths(installmentNumber - 1);
        }

        // 3. Set to dueDay, handling invalid dates (e.g., 31st in February)
        LocalDate dueDate = trySetDayOfMonth(baseDueDate, dueDay);
        
        // 4. For subsequent installments, recalculate reference month based on due date
        // Each installment belongs to the invoice of the month when it's due
        // The billing cycle logic: if due date is BEFORE closing day, it belongs to current month's invoice
        // If due date is ON or AFTER closing day, it belongs to current month's invoice (the month it vences)
        // CORRECTION: A parcel belongs to the invoice of the month it vences, not the next month
        // Example: If parcel vences on 10/02, it belongs to February invoice, not March
        if (installmentNumber > 1) {
            // For installments after the first, the invoice is the month when the installment is due
            // The closing day determines when the invoice closes, but the parcel belongs to the month it vences
            referenceMonth = dueDate.withDayOfMonth(1);
        }

        return new InstallmentDateInfo(installmentNumber, dueDate, referenceMonth);
    }

    /**
     * Try to set a specific day of month, falling back to the last day if invalid.
     *
     * Example: day=31, month=February → returns February 28/29
     *
     * @param date the base date
     * @param day  the desired day of month (1-31)
     * @return date with the day set, or last day of month if invalid
     */
    private LocalDate trySetDayOfMonth(LocalDate date, int day) {
        try {
            return date.withDayOfMonth(day);
        } catch (DateTimeException e) {
            // Day doesn't exist in this month (e.g., 31st in February)
            // Fall back to last day of the month
            return date.withDayOfMonth(date.lengthOfMonth());
        }
    }
}
