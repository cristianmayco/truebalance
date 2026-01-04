package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.domain.entity.CreditCard;
import com.truebalance.truebalance.domain.entity.Invoice;
import com.truebalance.truebalance.domain.port.CreditCardRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class AutoCloseInvoicesIfNeeded {

    private static final Logger logger = LoggerFactory.getLogger(AutoCloseInvoicesIfNeeded.class);

    private final CreditCardRepositoryPort creditCardRepository;
    private final CloseInvoice closeInvoice;

    public AutoCloseInvoicesIfNeeded(CreditCardRepositoryPort creditCardRepository,
                                     CloseInvoice closeInvoice) {
        this.creditCardRepository = creditCardRepository;
        this.closeInvoice = closeInvoice;
    }

    /**
     * Checks a list of invoices and automatically closes those whose closing date has passed.
     * The closing date is determined by the credit card's closingDay and the invoice's referenceMonth.
     *
     * @param invoices List of invoices to check
     */
    public void execute(List<Invoice> invoices) {
        LocalDate today = LocalDate.now();
        logger.info("AutoCloseInvoicesIfNeeded: Checking {} invoices (today: {})", invoices.size(), today);

        for (Invoice invoice : invoices) {
            logger.debug("Checking invoice {} (referenceMonth: {}, closed: {})",
                    invoice.getId(), invoice.getReferenceMonth(), invoice.isClosed());

            // Skip already closed invoices
            if (invoice.isClosed()) {
                continue;
            }

            // Get credit card to determine closing day
            Optional<CreditCard> creditCardOpt = creditCardRepository.findById(invoice.getCreditCardId());
            if (creditCardOpt.isEmpty()) {
                logger.warn("Credit card not found for invoice {}", invoice.getId());
                continue;
            }

            CreditCard creditCard = creditCardOpt.get();

            // Calculate closing date: referenceMonth + closingDay
            // Example: referenceMonth = 2024-12-01, closingDay = 21 -> closingDate = 2024-12-21
            LocalDate closingDate = invoice.getReferenceMonth().withDayOfMonth(creditCard.getClosingDay());

            logger.info("Invoice {} (referenceMonth: {}, closingDay: {}) -> closingDate: {}, today: {}, shouldClose: {}",
                    invoice.getId(), invoice.getReferenceMonth(), creditCard.getClosingDay(),
                    closingDate, today, !today.isBefore(closingDate));

            // If today is on or after the closing date, close the invoice
            if (!today.isBefore(closingDate)) {
                try {
                    logger.info("Auto-closing invoice {} for reference month {} (closing date: {})",
                            invoice.getId(), invoice.getReferenceMonth(), closingDate);
                    closeInvoice.execute(invoice.getId());
                } catch (Exception e) {
                    logger.error("Failed to auto-close invoice {}: {}", invoice.getId(), e.getMessage());
                }
            }
        }
    }
}
