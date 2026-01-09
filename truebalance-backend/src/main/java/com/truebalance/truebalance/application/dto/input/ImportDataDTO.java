package com.truebalance.truebalance.application.dto.input;

import jakarta.validation.Valid;
import java.util.List;

public class ImportDataDTO {
    @Valid
    private List<BillRequestDTO> bills;
    
    @Valid
    private List<CreditCardRequestDTO> creditCards;
    
    @Valid
    private List<InvoiceImportItemDTO> invoices;
    
    @Valid
    private List<CategoryRequestDTO> categories;

    public ImportDataDTO() {
    }

    public List<BillRequestDTO> getBills() {
        return bills;
    }

    public void setBills(List<BillRequestDTO> bills) {
        this.bills = bills;
    }

    public List<CreditCardRequestDTO> getCreditCards() {
        return creditCards;
    }

    public void setCreditCards(List<CreditCardRequestDTO> creditCards) {
        this.creditCards = creditCards;
    }

    public List<InvoiceImportItemDTO> getInvoices() {
        return invoices;
    }

    public void setInvoices(List<InvoiceImportItemDTO> invoices) {
        this.invoices = invoices;
    }

    public List<CategoryRequestDTO> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryRequestDTO> categories) {
        this.categories = categories;
    }
}
