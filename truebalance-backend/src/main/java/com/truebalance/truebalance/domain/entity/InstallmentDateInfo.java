package com.truebalance.truebalance.domain.entity;

import java.time.LocalDate;

/**
 * Helper DTO to hold calculated installment date information.
 * Used to pass data between InstallmentDateCalculator and CreateBillWithCreditCard.
 */
public class InstallmentDateInfo {

    private int installmentNumber;
    private LocalDate referenceMonth;  // First day of the month for the invoice
    private LocalDate dueDate;         // When this installment is due

    public InstallmentDateInfo() {
    }

    public InstallmentDateInfo(int installmentNumber, LocalDate dueDate, LocalDate referenceMonth) {
        this.installmentNumber = installmentNumber;
        this.dueDate = dueDate;
        this.referenceMonth = referenceMonth;
    }

    public int getInstallmentNumber() {
        return installmentNumber;
    }

    public void setInstallmentNumber(int installmentNumber) {
        this.installmentNumber = installmentNumber;
    }

    public LocalDate getReferenceMonth() {
        return referenceMonth;
    }

    public void setReferenceMonth(LocalDate referenceMonth) {
        this.referenceMonth = referenceMonth;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}
