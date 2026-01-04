package com.truebalance.truebalance.application.dto.output;

import java.util.List;

public class ProcessPostImportResultDTO {
    private int invoicesRecalculated;
    private int billsProcessed;
    private int errors;
    private List<String> errorMessages;

    public ProcessPostImportResultDTO() {
    }

    public ProcessPostImportResultDTO(int invoicesRecalculated, int billsProcessed, 
                                     int errors, List<String> errorMessages) {
        this.invoicesRecalculated = invoicesRecalculated;
        this.billsProcessed = billsProcessed;
        this.errors = errors;
        this.errorMessages = errorMessages;
    }

    public int getInvoicesRecalculated() {
        return invoicesRecalculated;
    }

    public void setInvoicesRecalculated(int invoicesRecalculated) {
        this.invoicesRecalculated = invoicesRecalculated;
    }

    public int getBillsProcessed() {
        return billsProcessed;
    }

    public void setBillsProcessed(int billsProcessed) {
        this.billsProcessed = billsProcessed;
    }

    public int getErrors() {
        return errors;
    }

    public void setErrors(int errors) {
        this.errors = errors;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(List<String> errorMessages) {
        this.errorMessages = errorMessages;
    }
}
