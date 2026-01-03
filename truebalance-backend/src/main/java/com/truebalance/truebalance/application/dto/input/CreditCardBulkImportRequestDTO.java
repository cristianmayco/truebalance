package com.truebalance.truebalance.application.dto.input;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class CreditCardBulkImportRequestDTO {

    public enum DuplicateStrategy {
        SKIP,
        CREATE_DUPLICATE
    }

    @NotEmpty(message = "A lista de itens não pode estar vazia")
    @Valid
    private List<CreditCardImportItemDTO> items;

    @NotNull(message = "Estratégia de duplicatas é obrigatória")
    private DuplicateStrategy duplicateStrategy;

    public CreditCardBulkImportRequestDTO() {
    }

    public CreditCardBulkImportRequestDTO(List<CreditCardImportItemDTO> items, DuplicateStrategy duplicateStrategy) {
        this.items = items;
        this.duplicateStrategy = duplicateStrategy;
    }

    public List<CreditCardImportItemDTO> getItems() {
        return items;
    }

    public void setItems(List<CreditCardImportItemDTO> items) {
        this.items = items;
    }

    public DuplicateStrategy getDuplicateStrategy() {
        return duplicateStrategy;
    }

    public void setDuplicateStrategy(DuplicateStrategy duplicateStrategy) {
        this.duplicateStrategy = duplicateStrategy;
    }
}
