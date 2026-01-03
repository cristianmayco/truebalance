package com.truebalance.truebalance.application.dto.output;

import com.truebalance.truebalance.domain.entity.Installment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for Installment entity.
 * Returns installment data to API clients.
 */
public class InstallmentResponseDTO {

    private Long id;
    private Long billId;
    private Long creditCardId;
    private Long invoiceId;
    private int installmentNumber;
    private BigDecimal amount;
    private LocalDate dueDate;
    private LocalDateTime createdAt;

    public InstallmentResponseDTO() {
    }

    public InstallmentResponseDTO(Long id, Long billId, Long creditCardId, Long invoiceId,
                                   int installmentNumber, BigDecimal amount, LocalDate dueDate,
                                   LocalDateTime createdAt) {
        this.id = id;
        this.billId = billId;
        this.creditCardId = creditCardId;
        this.invoiceId = invoiceId;
        this.installmentNumber = installmentNumber;
        this.amount = amount;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
    }

    /**
     * Converts a domain Installment entity to InstallmentResponseDTO.
     *
     * @param installment the domain entity
     * @return the response DTO
     */
    public static InstallmentResponseDTO fromInstallment(Installment installment) {
        return new InstallmentResponseDTO(
                installment.getId(),
                installment.getBillId(),
                installment.getCreditCardId(),
                installment.getInvoiceId(),
                installment.getInstallmentNumber(),
                installment.getAmount(),
                installment.getDueDate(),
                installment.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public Long getCreditCardId() {
        return creditCardId;
    }

    public void setCreditCardId(Long creditCardId) {
        this.creditCardId = creditCardId;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public int getInstallmentNumber() {
        return installmentNumber;
    }

    public void setInstallmentNumber(int installmentNumber) {
        this.installmentNumber = installmentNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
