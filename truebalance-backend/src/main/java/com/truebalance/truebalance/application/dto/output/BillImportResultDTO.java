package com.truebalance.truebalance.application.dto.output;

import java.util.ArrayList;
import java.util.List;

public class BillImportResultDTO {

    private Integer totalProcessed;
    private Integer totalCreated;
    private Integer totalSkipped;
    private Integer totalErrors;
    private List<DuplicateInfoDTO> duplicatesFound;
    private List<ImportErrorDTO> errors;
    private List<BillResponseDTO> createdBills;

    public BillImportResultDTO() {
        this.duplicatesFound = new ArrayList<>();
        this.errors = new ArrayList<>();
        this.createdBills = new ArrayList<>();
    }

    public BillImportResultDTO(Integer totalProcessed, Integer totalCreated, Integer totalSkipped,
                               Integer totalErrors, List<DuplicateInfoDTO> duplicatesFound,
                               List<ImportErrorDTO> errors, List<BillResponseDTO> createdBills) {
        this.totalProcessed = totalProcessed;
        this.totalCreated = totalCreated;
        this.totalSkipped = totalSkipped;
        this.totalErrors = totalErrors;
        this.duplicatesFound = duplicatesFound != null ? duplicatesFound : new ArrayList<>();
        this.errors = errors != null ? errors : new ArrayList<>();
        this.createdBills = createdBills != null ? createdBills : new ArrayList<>();
    }

    // Getters and Setters
    public Integer getTotalProcessed() {
        return totalProcessed;
    }

    public void setTotalProcessed(Integer totalProcessed) {
        this.totalProcessed = totalProcessed;
    }

    public Integer getTotalCreated() {
        return totalCreated;
    }

    public void setTotalCreated(Integer totalCreated) {
        this.totalCreated = totalCreated;
    }

    public Integer getTotalSkipped() {
        return totalSkipped;
    }

    public void setTotalSkipped(Integer totalSkipped) {
        this.totalSkipped = totalSkipped;
    }

    public Integer getTotalErrors() {
        return totalErrors;
    }

    public void setTotalErrors(Integer totalErrors) {
        this.totalErrors = totalErrors;
    }

    public List<DuplicateInfoDTO> getDuplicatesFound() {
        return duplicatesFound;
    }

    public void setDuplicatesFound(List<DuplicateInfoDTO> duplicatesFound) {
        this.duplicatesFound = duplicatesFound;
    }

    public List<ImportErrorDTO> getErrors() {
        return errors;
    }

    public void setErrors(List<ImportErrorDTO> errors) {
        this.errors = errors;
    }

    public List<BillResponseDTO> getCreatedBills() {
        return createdBills;
    }

    public void setCreatedBills(List<BillResponseDTO> createdBills) {
        this.createdBills = createdBills;
    }
}
