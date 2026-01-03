package com.truebalance.truebalance.application.dto.output;

import com.truebalance.truebalance.domain.entity.PartialPayment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for PartialPayment entity.
 * Returns partial payment data to API clients.
 * BR-PP-006: Includes creditCardAvailableLimit for real-time limit updates
 */
public class PartialPaymentResponseDTO {

    private Long id;
    private Long invoiceId;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private String description;
    private LocalDateTime createdAt;
    private BigDecimal creditCardAvailableLimit;  // BR-PP-006: Real-time limit update

    public PartialPaymentResponseDTO() {
    }

    public PartialPaymentResponseDTO(Long id, Long invoiceId, BigDecimal amount,
                                      LocalDateTime paymentDate, String description,
                                      LocalDateTime createdAt, BigDecimal creditCardAvailableLimit) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.description = description;
        this.createdAt = createdAt;
        this.creditCardAvailableLimit = creditCardAvailableLimit;
    }

    /**
     * Converts a domain PartialPayment entity to PartialPaymentResponseDTO.
     * Note: creditCardAvailableLimit must be set separately by the use case.
     *
     * @param partialPayment the domain entity
     * @return the response DTO (without creditCardAvailableLimit)
     */
    public static PartialPaymentResponseDTO fromPartialPayment(PartialPayment partialPayment) {
        return new PartialPaymentResponseDTO(
                partialPayment.getId(),
                partialPayment.getInvoiceId(),
                partialPayment.getAmount(),
                partialPayment.getPaymentDate(),
                partialPayment.getDescription(),
                partialPayment.getCreatedAt(),
                null  // creditCardAvailableLimit must be set by use case
        );
    }

    /**
     * Converts a domain PartialPayment entity to PartialPaymentResponseDTO with available limit.
     *
     * @param partialPayment the domain entity
     * @param availableLimit the calculated available limit
     * @return the response DTO
     */
    public static PartialPaymentResponseDTO fromPartialPaymentWithLimit(PartialPayment partialPayment,
                                                                         BigDecimal availableLimit) {
        return new PartialPaymentResponseDTO(
                partialPayment.getId(),
                partialPayment.getInvoiceId(),
                partialPayment.getAmount(),
                partialPayment.getPaymentDate(),
                partialPayment.getDescription(),
                partialPayment.getCreatedAt(),
                availableLimit
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public BigDecimal getCreditCardAvailableLimit() {
        return creditCardAvailableLimit;
    }

    public void setCreditCardAvailableLimit(BigDecimal creditCardAvailableLimit) {
        this.creditCardAvailableLimit = creditCardAvailableLimit;
    }
}
