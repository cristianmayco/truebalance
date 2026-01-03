package com.truebalance.truebalance.application.dto.output;

import java.util.ArrayList;
import java.util.List;

public class InvoiceImportResultDTO {

    private Integer totalProcessed;
    private Integer totalCreated;
    private Integer totalSkipped;
    private Integer totalErrors;
    private List<InvoiceDuplicateInfoDTO> duplicatesFound;
    private List<ImportErrorDTO> errors;
    private List<InvoiceResponseDTO> createdInvoices;

    public InvoiceImportResultDTO() {
        this.duplicatesFound = new ArrayList<>();
        this.errors = new ArrayList<>();
        this.createdInvoices = new ArrayList<>();
    }

    public InvoiceImportResultDTO(Integer totalProcessed, Integer totalCreated, Integer totalSkipped,
                                  Integer totalErrors, List<InvoiceDuplicateInfoDTO> duplicatesFound,
                                  List<ImportErrorDTO> errors, List<InvoiceResponseDTO> createdInvoices) {
        this.totalProcessed = totalProcessed;
        this.totalCreated = totalCreated;
        this.totalSkipped = totalSkipped;
        this.totalErrors = totalErrors;
        this.duplicatesFound = duplicatesFound != null ? duplicatesFound : new ArrayList<>();
        this.errors = errors != null ? errors : new ArrayList<>();
        this.createdInvoices = createdInvoices != null ? createdInvoices : new ArrayList<>();
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

    public List<InvoiceDuplicateInfoDTO> getDuplicatesFound() {
        return duplicatesFound;
    }

    public void setDuplicatesFound(List<InvoiceDuplicateInfoDTO> duplicatesFound) {
        this.duplicatesFound = duplicatesFound;
    }

    public List<ImportErrorDTO> getErrors() {
        return errors;
    }

    public void setErrors(List<ImportErrorDTO> errors) {
        this.errors = errors;
    }

    public List<InvoiceResponseDTO> getCreatedInvoices() {
        return createdInvoices;
    }

    public void setCreatedInvoices(List<InvoiceResponseDTO> createdInvoices) {
        this.createdInvoices = createdInvoices;
    }
}
