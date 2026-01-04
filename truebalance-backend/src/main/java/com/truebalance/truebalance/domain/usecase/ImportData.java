package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.application.dto.input.BillRequestDTO;
import com.truebalance.truebalance.application.dto.input.CreditCardRequestDTO;
import com.truebalance.truebalance.application.dto.input.ImportDataDTO;
import com.truebalance.truebalance.application.dto.input.InvoiceImportItemDTO;
import com.truebalance.truebalance.application.dto.output.ImportResultDTO;
import com.truebalance.truebalance.domain.entity.Bill;
import com.truebalance.truebalance.domain.entity.CreditCard;
import com.truebalance.truebalance.domain.entity.Invoice;
import com.truebalance.truebalance.domain.port.BillRepositoryPort;
import com.truebalance.truebalance.domain.service.CreditCardNameNormalizer;
import com.truebalance.truebalance.domain.port.CreditCardRepositoryPort;
import com.truebalance.truebalance.domain.port.InvoiceRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ImportData {

    private static final Logger logger = LoggerFactory.getLogger(ImportData.class);
    private final BillRepositoryPort billRepository;
    private final CreditCardRepositoryPort creditCardRepository;
    private final InvoiceRepositoryPort invoiceRepository;
    private final CreateBill createBill;
    private final CreateCreditCard createCreditCard;
    private final CreateBillWithCreditCard createBillWithCreditCard;
    private final ProcessPostImport processPostImport;

    public ImportData(BillRepositoryPort billRepository,
                      CreditCardRepositoryPort creditCardRepository,
                      InvoiceRepositoryPort invoiceRepository,
                      CreateBill createBill,
                      CreateCreditCard createCreditCard,
                      CreateBillWithCreditCard createBillWithCreditCard,
                      ProcessPostImport processPostImport) {
        this.billRepository = billRepository;
        this.creditCardRepository = creditCardRepository;
        this.invoiceRepository = invoiceRepository;
        this.createBill = createBill;
        this.createCreditCard = createCreditCard;
        this.createBillWithCreditCard = createBillWithCreditCard;
        this.processPostImport = processPostImport;
    }

    public ImportResultDTO execute(ImportDataDTO importData) {
        logger.info("Iniciando importação de dados");
        List<String> errors = new ArrayList<>();
        int totalProcessed = 0;
        int totalCreated = 0;
        int totalSkipped = 0;
        
        // Mapeamento de IDs antigos (do JSON) para novos IDs (gerados pelo banco)
        Map<Long, Long> creditCardIdMapping = new HashMap<>();

        // Validar dependências antes de importar
        if (importData.getInvoices() != null && !importData.getInvoices().isEmpty() &&
            (importData.getCreditCards() == null || importData.getCreditCards().isEmpty())) {
            // Verificar se todas as faturas referenciam cartões existentes no banco
            List<Long> requiredCreditCardIds = importData.getInvoices().stream()
                    .map(InvoiceImportItemDTO::getCreditCardId)
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());
            
            List<CreditCard> existingCreditCards = creditCardRepository.findAll();
            List<Long> existingCreditCardIds = existingCreditCards.stream()
                    .map(CreditCard::getId)
                    .collect(java.util.stream.Collectors.toList());
            
            List<Long> missingCreditCardIds = requiredCreditCardIds.stream()
                    .filter(id -> !existingCreditCardIds.contains(id))
                    .collect(java.util.stream.Collectors.toList());
            
            if (!missingCreditCardIds.isEmpty()) {
                String warning = String.format(
                    "Aviso: %d fatura(s) referenciam cartões que não existem no banco de dados (IDs: %s). " +
                    "Essas faturas serão ignoradas. Certifique-se de importar os cartões de crédito antes das faturas.",
                    importData.getInvoices().stream()
                            .filter(inv -> missingCreditCardIds.contains(inv.getCreditCardId()))
                            .count(),
                    missingCreditCardIds.toString()
                );
                logger.warn(warning);
                // Não adicionar como erro, apenas logar como aviso
            }
        }

        // Importar cartões de crédito primeiro (pois faturas dependem deles)
        if (importData.getCreditCards() != null && !importData.getCreditCards().isEmpty()) {
            logger.info("Importando {} cartões de crédito", importData.getCreditCards().size());
            for (CreditCardRequestDTO dto : importData.getCreditCards()) {
                totalProcessed++;
                try {
                    Long oldId = dto.getId(); // ID do JSON (pode ser null)
                    
                    // Verificar se já existe cartão com mesmo nome (normalizado)
                    String normalizedName = CreditCardNameNormalizer.normalize(dto.getName());
                    Optional<CreditCard> existing = creditCardRepository.findAll().stream()
                            .filter(cc -> CreditCardNameNormalizer.normalize(cc.getName()).equals(normalizedName))
                            .findFirst();

                    if (existing.isPresent()) {
                        logger.debug("Cartão '{}' já existe, ignorando", dto.getName());
                        // Se havia um ID antigo, mapear para o ID existente
                        if (oldId != null) {
                            creditCardIdMapping.put(oldId, existing.get().getId());
                            logger.debug("Mapeando ID antigo {} para ID existente {} para cartão '{}'", 
                                    oldId, existing.get().getId(), dto.getName());
                        }
                        totalSkipped++;
                    } else {
                        CreditCard creditCard = dto.toCreditCard();
                        CreditCard savedCreditCard = createCreditCard.execute(creditCard);
                        // Se havia um ID antigo, mapear para o novo ID gerado
                        if (oldId != null) {
                            creditCardIdMapping.put(oldId, savedCreditCard.getId());
                            logger.debug("Mapeando ID antigo {} para novo ID {} para cartão '{}'", 
                                    oldId, savedCreditCard.getId(), dto.getName());
                        }
                        totalCreated++;
                        logger.debug("Cartão '{}' criado com sucesso", dto.getName());
                    }
                } catch (Exception e) {
                    totalSkipped++;
                    String error = String.format("Erro ao importar cartão '%s': %s", dto.getName(), e.getMessage());
                    errors.add(error);
                    logger.error(error, e);
                }
            }
        }

        // Importar contas (após cartões, pois contas podem referenciar cartões)
        if (importData.getBills() != null && !importData.getBills().isEmpty()) {
            logger.info("Importando {} contas", importData.getBills().size());
            for (BillRequestDTO dto : importData.getBills()) {
                totalProcessed++;
                try {
                    Bill bill = dto.toBill();
                    Long creditCardId = null;
                    
                    // Verificar se conta referencia um cartão e se esse cartão existe
                    if (dto.getCreditCardId() != null) {
                        creditCardId = dto.getCreditCardId();
                        // Se houver mapeamento, usar o ID mapeado
                        if (creditCardIdMapping.containsKey(creditCardId)) {
                            creditCardId = creditCardIdMapping.get(creditCardId);
                            logger.debug("Atualizando referência de cartão de crédito de {} para {} na conta '{}'", 
                                    dto.getCreditCardId(), creditCardId, dto.getName());
                        }
                        
                        Optional<CreditCard> creditCard = creditCardRepository.findById(creditCardId);
                        if (creditCard.isEmpty()) {
                            totalSkipped++;
                            String error = String.format("Conta '%s': Cartão de crédito ID=%d não encontrado. " +
                                    "Certifique-se de importar os cartões de crédito antes das contas.",
                                    dto.getName(), creditCardId);
                            errors.add(error);
                            logger.warn(error);
                            continue;
                        }
                        
                        // Usar CreateBillWithCreditCard para criar vínculo e parcelas
                        logger.debug("Criando conta '{}' vinculada ao cartão de crédito ID={}", 
                                dto.getName(), creditCardId);
                        createBillWithCreditCard.execute(bill, creditCardId);
                    } else {
                        // Conta sem cartão de crédito
                        createBill.addBill(bill);
                    }
                    totalCreated++;
                    logger.debug("Conta '{}' criada com sucesso", dto.getName());
                } catch (Exception e) {
                    totalSkipped++;
                    String error = String.format("Erro ao importar conta '%s': %s", dto.getName(), e.getMessage());
                    errors.add(error);
                    logger.error(error, e);
                }
            }
        }

        // Importar faturas
        if (importData.getInvoices() != null && !importData.getInvoices().isEmpty()) {
            logger.info("Importando {} faturas", importData.getInvoices().size());
            for (InvoiceImportItemDTO dto : importData.getInvoices()) {
                totalProcessed++;
                try {
                    Long creditCardId = dto.getCreditCardId();
                    // Se houver mapeamento, usar o ID mapeado
                    if (creditCardIdMapping.containsKey(creditCardId)) {
                        creditCardId = creditCardIdMapping.get(creditCardId);
                        logger.debug("Atualizando referência de cartão de crédito de {} para {} na fatura do mês {}", 
                                dto.getCreditCardId(), creditCardId, dto.getReferenceMonth());
                    }
                    
                    // Verificar se cartão existe
                    Optional<CreditCard> creditCard = creditCardRepository.findById(creditCardId);
                    if (creditCard.isEmpty()) {
                        totalSkipped++;
                        String error = String.format("Fatura do mês %s: Cartão de crédito ID=%d não encontrado. " +
                                "Certifique-se de importar os cartões de crédito antes das faturas.",
                                dto.getReferenceMonth(), creditCardId);
                        errors.add(error);
                        logger.warn(error);
                        continue;
                    }

                    // Verificar se fatura já existe
                    Optional<Invoice> existing = invoiceRepository.findByCreditCardIdAndReferenceMonth(
                            creditCardId, dto.getReferenceMonth());

                    if (existing.isPresent()) {
                        logger.debug("Fatura para cartão ID={} e mês {} já existe, ignorando",
                                creditCardId, dto.getReferenceMonth());
                        totalSkipped++;
                    } else {
                        Invoice invoice = new Invoice();
                        invoice.setCreditCardId(creditCardId);
                        invoice.setReferenceMonth(dto.getReferenceMonth());
                        invoice.setTotalAmount(dto.getTotalAmount());
                        invoice.setPreviousBalance(dto.getPreviousBalance() != null ? dto.getPreviousBalance() : BigDecimal.ZERO);
                        invoice.setClosed(dto.getClosed() != null ? dto.getClosed() : false);
                        invoice.setPaid(dto.getPaid() != null ? dto.getPaid() : false);
                        invoice.setUseAbsoluteValue(dto.getUseAbsoluteValue() != null ? dto.getUseAbsoluteValue() : false);
                        invoiceRepository.save(invoice);
                        totalCreated++;
                        logger.debug("Fatura criada com sucesso para cartão ID={}, mês {}",
                                creditCardId, dto.getReferenceMonth());
                    }
                } catch (Exception e) {
                    totalSkipped++;
                    Long creditCardId = creditCardIdMapping.getOrDefault(dto.getCreditCardId(), dto.getCreditCardId());
                    String error = String.format("Erro ao importar fatura (cartão ID=%d, mês=%s): %s",
                            creditCardId, dto.getReferenceMonth(), e.getMessage());
                    errors.add(error);
                    logger.error(error, e);
                }
            }
        }

        ImportResultDTO result = new ImportResultDTO(
                totalProcessed,
                totalCreated,
                totalSkipped,
                errors.size(),
                errors
        );

        logger.info("Importação concluída: {} processados, {} criados, {} ignorados, {} erros",
                totalProcessed, totalCreated, totalSkipped, errors.size());

        // Processar pós-importação automaticamente para corrigir vínculos e recalcular faturas
        try {
            logger.info("Iniciando processamento pós-importação automático");
            ProcessPostImport.ProcessPostImportResult postImportResult = processPostImport.execute();
            logger.info("Processamento pós-importação concluído: {} faturas recalculadas, {} contas processadas",
                    postImportResult.getInvoicesRecalculated(), postImportResult.getBillsProcessed());
            
            if (postImportResult.getErrors() > 0) {
                logger.warn("Processamento pós-importação teve {} erros: {}", 
                        postImportResult.getErrors(), postImportResult.getErrorMessages());
                // Adicionar erros do processamento pós-importação aos erros da importação
                errors.addAll(postImportResult.getErrorMessages());
                result.setTotalErrors(errors.size());
                result.setErrors(errors);
            }
        } catch (Exception e) {
            logger.error("Erro durante processamento pós-importação automático", e);
            String errorMsg = "Erro no processamento pós-importação: " + e.getMessage();
            errors.add(errorMsg);
            result.setTotalErrors(errors.size());
            result.setErrors(errors);
        }

        return result;
    }
}
