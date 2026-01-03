package com.truebalance.truebalance.application.dto.input;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class InvoiceBulkImportRequestDTO {

    public enum DuplicateStrategy {
        SKIP,
        CREATE_DUPLICATE
    }

    @NotEmpty(message = "A lista de itens não pode estar vazia")
    @Valid
    private List<InvoiceImportItemDTO> items;

    @NotNull(message = "Estratégia de duplicatas é obrigatória")
    private DuplicateStrategy duplicateStrategy;

    public InvoiceBulkImportRequestDTO() {
    }

    public InvoiceBulkImportRequestDTO(List<InvoiceImportItemDTO> items, DuplicateStrategy duplicateStrategy) {
        this.items = items;
        this.duplicateStrategy = duplicateStrategy;
    }

    // Getters and Setters
    public List<InvoiceImportItemDTO> getItems() {
        return items;
    }

    public void setItems(List<InvoiceImportItemDTO> items) {
        this.items = items;
    }

    public DuplicateStrategy getDuplicateStrategy() {
        return duplicateStrategy;
    }

    public void setDuplicateStrategy(DuplicateStrategy duplicateStrategy) {
        this.duplicateStrategy = duplicateStrategy;
    }
}
