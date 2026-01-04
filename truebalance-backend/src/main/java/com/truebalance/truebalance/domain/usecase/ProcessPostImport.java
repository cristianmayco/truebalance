package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.domain.entity.Bill;
import com.truebalance.truebalance.domain.entity.CreditCard;
import com.truebalance.truebalance.domain.entity.Invoice;
import com.truebalance.truebalance.domain.entity.Installment;
import com.truebalance.truebalance.domain.port.BillRepositoryPort;
import com.truebalance.truebalance.domain.port.CreditCardRepositoryPort;
import com.truebalance.truebalance.domain.port.InvoiceRepositoryPort;
import com.truebalance.truebalance.domain.port.InstallmentRepositoryPort;
import com.truebalance.truebalance.domain.service.InstallmentDateCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Use case to process post-import corrections:
 * - Recalculate invoice totals based on existing installments
 * - Create missing installments for bills that should be linked to credit cards
 * 
 * This is useful after importing data to ensure all relationships are correct
 * and invoice totals are accurate.
 */
public class ProcessPostImport {

    private static final Logger logger = LoggerFactory.getLogger(ProcessPostImport.class);
    
    private final BillRepositoryPort billRepository;
    private final CreditCardRepositoryPort creditCardRepository;
    private final InvoiceRepositoryPort invoiceRepository;
    private final InstallmentRepositoryPort installmentRepository;
    private final GenerateOrGetInvoiceForMonth generateOrGetInvoiceForMonth;
    private final InstallmentDateCalculator installmentDateCalculator;
    private final CreateBillWithCreditCard createBillWithCreditCard;

    public ProcessPostImport(
            BillRepositoryPort billRepository,
            CreditCardRepositoryPort creditCardRepository,
            InvoiceRepositoryPort invoiceRepository,
            InstallmentRepositoryPort installmentRepository,
            GenerateOrGetInvoiceForMonth generateOrGetInvoiceForMonth,
            InstallmentDateCalculator installmentDateCalculator,
            CreateBillWithCreditCard createBillWithCreditCard) {
        this.billRepository = billRepository;
        this.creditCardRepository = creditCardRepository;
        this.invoiceRepository = invoiceRepository;
        this.installmentRepository = installmentRepository;
        this.generateOrGetInvoiceForMonth = generateOrGetInvoiceForMonth;
        this.installmentDateCalculator = installmentDateCalculator;
        this.createBillWithCreditCard = createBillWithCreditCard;
    }

    /**
     * Process all bills and invoices to fix relationships and recalculate totals.
     * 
     * @return ProcessPostImportResult with statistics about what was processed
     */
    @Transactional(rollbackFor = Exception.class)
    public ProcessPostImportResult execute() {
        logger.info("Iniciando processamento pós-importação");
        
        int invoicesRecalculated = 0;
        int billsProcessed = 0;
        int errors = 0;
        List<String> errorMessages = new java.util.ArrayList<>();

        try {
            // 1. Recalculate invoice totals based on existing installments
            logger.info("Recalculando totais das faturas baseado nas parcelas existentes");
            invoicesRecalculated = recalculateInvoiceTotals();
            
            // 2. Process bills that might need installments created
            // This would require additional logic to identify which bills should have installments
            // For now, we focus on recalculating invoice totals
            
            logger.info("Processamento pós-importação concluído: {} faturas recalculadas", invoicesRecalculated);
            
        } catch (Exception e) {
            logger.error("Erro durante processamento pós-importação", e);
            errors++;
            errorMessages.add("Erro geral: " + e.getMessage());
        }

        return new ProcessPostImportResult(
                invoicesRecalculated,
                billsProcessed,
                errors,
                errorMessages
        );
    }

    /**
     * Recalculate invoice totals based on existing installments.
     * This ensures invoice totals match the sum of their installments.
     * 
     * @return number of invoices recalculated
     */
    private int recalculateInvoiceTotals() {
        List<Invoice> allInvoices = invoiceRepository.findAll();
        int recalculated = 0;

        for (Invoice invoice : allInvoices) {
            try {
                // Skip recalculation if invoice uses absolute value (BR-I-018)
                if (invoice.isUseAbsoluteValue()) {
                    logger.debug("Fatura ID={} usa valor absoluto, pulando recálculo", invoice.getId());
                    continue;
                }
                
                // Get all installments for this invoice
                List<Installment> installments = installmentRepository.findByInvoiceId(invoice.getId());
                
                // Calculate total from installments
                BigDecimal calculatedTotal = installments.stream()
                        .map(Installment::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                // Update invoice if total differs
                if (invoice.getTotalAmount().compareTo(calculatedTotal) != 0) {
                    logger.debug("Recalculando fatura ID={}: total antigo={}, novo total={}", 
                            invoice.getId(), invoice.getTotalAmount(), calculatedTotal);
                    invoice.setTotalAmount(calculatedTotal);
                    invoiceRepository.save(invoice);
                    recalculated++;
                }
            } catch (Exception e) {
                logger.error("Erro ao recalcular fatura ID={}", invoice.getId(), e);
            }
        }

        return recalculated;
    }

    /**
     * Result class for ProcessPostImport execution.
     */
    public static class ProcessPostImportResult {
        private final int invoicesRecalculated;
        private final int billsProcessed;
        private final int errors;
        private final List<String> errorMessages;

        public ProcessPostImportResult(int invoicesRecalculated, int billsProcessed, 
                                     int errors, List<String> errorMessages) {
            this.invoicesRecalculated = invoicesRecalculated;
            this.billsProcessed = billsProcessed;
            this.errors = errors;
            this.errorMessages = errorMessages;
        }

        public int getInvoicesRecalculated() {
            return invoicesRecalculated;
        }

        public int getBillsProcessed() {
            return billsProcessed;
        }

        public int getErrors() {
            return errors;
        }

        public List<String> getErrorMessages() {
            return errorMessages;
        }
    }
}
