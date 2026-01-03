package com.truebalance.truebalance.application.dto.input;

import com.truebalance.truebalance.domain.entity.PartialPayment;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Request DTO for registering a partial payment on an invoice.
 * BR-PP-001: Can only be used on open invoices with allowsPartialPayment = true
 * BR-PP-002: Amount can exceed invoice balance (creates credit)
 */
public class PartialPaymentRequestDTO {

    @NotNull(message = "Valor do pagamento é obrigatório")
    @Positive(message = "Valor do pagamento deve ser positivo")
    private BigDecimal amount;

    private String description;  // Optional

    public PartialPaymentRequestDTO() {
    }

    public PartialPaymentRequestDTO(BigDecimal amount, String description) {
        this.amount = amount;
        this.description = description;
    }

    /**
     * Converts this DTO to a domain PartialPayment entity.
     * Note: paymentDate is set by the use case to LocalDateTime.now()
     *
     * @return domain entity
     */
    public PartialPayment toPartialPayment() {
        PartialPayment partialPayment = new PartialPayment();
        partialPayment.setAmount(amount);
        partialPayment.setDescription(description);
        // invoiceId and paymentDate are set by the use case
        return partialPayment;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
