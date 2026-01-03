package com.truebalance.truebalance.application.dto.output;

import java.util.List;

public class ImportResultDTO {
    private int totalProcessed;
    private int totalCreated;
    private int totalSkipped;
    private int totalErrors;
    private List<String> errors;

    public ImportResultDTO() {
    }

    public ImportResultDTO(int totalProcessed, int totalCreated, int totalSkipped, int totalErrors, List<String> errors) {
        this.totalProcessed = totalProcessed;
        this.totalCreated = totalCreated;
        this.totalSkipped = totalSkipped;
        this.totalErrors = totalErrors;
        this.errors = errors;
    }

    public int getTotalProcessed() {
        return totalProcessed;
    }

    public void setTotalProcessed(int totalProcessed) {
        this.totalProcessed = totalProcessed;
    }

    public int getTotalCreated() {
        return totalCreated;
    }

    public void setTotalCreated(int totalCreated) {
        this.totalCreated = totalCreated;
    }

    public int getTotalSkipped() {
        return totalSkipped;
    }

    public void setTotalSkipped(int totalSkipped) {
        this.totalSkipped = totalSkipped;
    }

    public int getTotalErrors() {
        return totalErrors;
    }

    public void setTotalErrors(int totalErrors) {
        this.totalErrors = totalErrors;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
