package com.truebalance.truebalance.application.dto.output;

import java.util.List;

public class ExportDataDTO {
    private List<BillResponseDTO> bills;
    private List<CreditCardResponseDTO> creditCards;
    private List<InvoiceResponseDTO> invoices;
    private List<CategoryResponseDTO> categories;

    public ExportDataDTO() {
    }

    public ExportDataDTO(List<BillResponseDTO> bills, List<CreditCardResponseDTO> creditCards, List<InvoiceResponseDTO> invoices, List<CategoryResponseDTO> categories) {
        this.bills = bills;
        this.creditCards = creditCards;
        this.invoices = invoices;
        this.categories = categories;
    }

    public List<BillResponseDTO> getBills() {
        return bills;
    }

    public void setBills(List<BillResponseDTO> bills) {
        this.bills = bills;
    }

    public List<CreditCardResponseDTO> getCreditCards() {
        return creditCards;
    }

    public void setCreditCards(List<CreditCardResponseDTO> creditCards) {
        this.creditCards = creditCards;
    }

    public List<InvoiceResponseDTO> getInvoices() {
        return invoices;
    }

    public void setInvoices(List<InvoiceResponseDTO> invoices) {
        this.invoices = invoices;
    }

    public List<CategoryResponseDTO> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryResponseDTO> categories) {
        this.categories = categories;
    }
}
