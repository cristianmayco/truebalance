package com.truebalance.truebalance.application.dto.output;

import java.util.ArrayList;
import java.util.List;

public class CreditCardImportResultDTO {

    private Integer totalProcessed;
    private Integer totalCreated;
    private Integer totalSkipped;
    private Integer totalErrors;
    private List<CreditCardDuplicateInfoDTO> duplicatesFound;
    private List<ImportErrorDTO> errors;
    private List<CreditCardResponseDTO> createdCreditCards;

    public CreditCardImportResultDTO() {
        this.duplicatesFound = new ArrayList<>();
        this.errors = new ArrayList<>();
        this.createdCreditCards = new ArrayList<>();
    }

    public CreditCardImportResultDTO(Integer totalProcessed, Integer totalCreated, Integer totalSkipped,
                                     Integer totalErrors, List<CreditCardDuplicateInfoDTO> duplicatesFound,
                                     List<ImportErrorDTO> errors, List<CreditCardResponseDTO> createdCreditCards) {
        this.totalProcessed = totalProcessed;
        this.totalCreated = totalCreated;
        this.totalSkipped = totalSkipped;
        this.totalErrors = totalErrors;
        this.duplicatesFound = duplicatesFound != null ? duplicatesFound : new ArrayList<>();
        this.errors = errors != null ? errors : new ArrayList<>();
        this.createdCreditCards = createdCreditCards != null ? createdCreditCards : new ArrayList<>();
    }

    public Integer getTotalProcessed() { return totalProcessed; }
    public void setTotalProcessed(Integer totalProcessed) { this.totalProcessed = totalProcessed; }
    public Integer getTotalCreated() { return totalCreated; }
    public void setTotalCreated(Integer totalCreated) { this.totalCreated = totalCreated; }
    public Integer getTotalSkipped() { return totalSkipped; }
    public void setTotalSkipped(Integer totalSkipped) { this.totalSkipped = totalSkipped; }
    public Integer getTotalErrors() { return totalErrors; }
    public void setTotalErrors(Integer totalErrors) { this.totalErrors = totalErrors; }
    public List<CreditCardDuplicateInfoDTO> getDuplicatesFound() { return duplicatesFound; }
    public void setDuplicatesFound(List<CreditCardDuplicateInfoDTO> duplicatesFound) { this.duplicatesFound = duplicatesFound; }
    public List<ImportErrorDTO> getErrors() { return errors; }
    public void setErrors(List<ImportErrorDTO> errors) { this.errors = errors; }
    public List<CreditCardResponseDTO> getCreatedCreditCards() { return createdCreditCards; }
    public void setCreatedCreditCards(List<CreditCardResponseDTO> createdCreditCards) { this.createdCreditCards = createdCreditCards; }
}
