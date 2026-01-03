package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.application.dto.input.InvoiceBulkImportRequestDTO;
import com.truebalance.truebalance.application.dto.input.InvoiceImportItemDTO;
import com.truebalance.truebalance.application.dto.output.ImportErrorDTO;
import com.truebalance.truebalance.application.dto.output.InvoiceDuplicateInfoDTO;
import com.truebalance.truebalance.application.dto.output.InvoiceImportResultDTO;
import com.truebalance.truebalance.application.dto.output.InvoiceResponseDTO;
import com.truebalance.truebalance.domain.entity.Invoice;
import com.truebalance.truebalance.domain.port.InvoiceRepositoryPort;
import com.truebalance.truebalance.infra.db.entity.InvoiceEntity;
import com.truebalance.truebalance.infra.db.repository.InvoiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ImportInvoicesInBulk {

    private static final Logger logger = LoggerFactory.getLogger(ImportInvoicesInBulk.class);

    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final InvoiceRepository invoiceRepository;

    public ImportInvoicesInBulk(InvoiceRepositoryPort invoiceRepositoryPort,
                                InvoiceRepository invoiceRepository) {
        this.invoiceRepositoryPort = invoiceRepositoryPort;
        this.invoiceRepository = invoiceRepository;
    }

    public InvoiceImportResultDTO execute(InvoiceBulkImportRequestDTO request) {
        logger.info("Iniciando importação em massa de {} faturas com estratégia: {}",
                request.getItems().size(), request.getDuplicateStrategy());

        List<InvoiceDuplicateInfoDTO> duplicatesFound = new ArrayList<>();
        List<ImportErrorDTO> errors = new ArrayList<>();
        List<InvoiceResponseDTO> createdInvoices = new ArrayList<>();

        int totalProcessed = request.getItems().size();
        int totalCreated = 0;
        int totalSkipped = 0;
        int totalErrors = 0;

        for (InvoiceImportItemDTO item : request.getItems()) {
            try {
                logger.debug("Processando item da linha {}: Cartão ID={}, Mês={}",
                        item.getLineNumber(), item.getCreditCardId(), item.getReferenceMonth());

                // Verificar duplicata (mesmo cartão + mesmo mês)
                Optional<InvoiceEntity> duplicateEntity = invoiceRepository.findByCreditCardIdAndReferenceMonth(
                        item.getCreditCardId(),
                        item.getReferenceMonth()
                );

                if (duplicateEntity.isPresent()) {
                    logger.debug("Duplicata encontrada para linha {}: Invoice ID={}",
                            item.getLineNumber(), duplicateEntity.get().getId());

                    if (request.getDuplicateStrategy() == InvoiceBulkImportRequestDTO.DuplicateStrategy.SKIP) {
                        // Adicionar em duplicatas e pular
                        InvoiceDuplicateInfoDTO duplicateInfo = new InvoiceDuplicateInfoDTO(
                                item.getLineNumber(),
                                item.getCreditCardId(),
                                item.getReferenceMonth(),
                                item.getTotalAmount(),
                                duplicateEntity.get().getId(),
                                String.format("Duplicata encontrada: já existe uma fatura para o cartão ID %d no mês %s (ID: %d)",
                                        item.getCreditCardId(), item.getReferenceMonth(), duplicateEntity.get().getId())
                        );
                        duplicatesFound.add(duplicateInfo);
                        totalSkipped++;
                        logger.debug("Fatura da linha {} ignorada (duplicata)", item.getLineNumber());
                        continue;
                    } else {
                        logger.debug("Criando duplicata para linha {} conforme estratégia CREATE_DUPLICATE",
                                item.getLineNumber());
                    }
                }

                // Criar invoice
                Invoice invoice = item.toInvoice();
                Invoice createdInvoice = invoiceRepositoryPort.save(invoice);

                createdInvoices.add(InvoiceResponseDTO.fromInvoice(createdInvoice));
                totalCreated++;
                logger.debug("Fatura da linha {} criada com sucesso! ID={}",
                        item.getLineNumber(), createdInvoice.getId());

            } catch (Exception e) {
                logger.error("Erro ao processar item da linha {}: {}", item.getLineNumber(), e.getMessage(), e);

                ImportErrorDTO error = new ImportErrorDTO(
                        item.getLineNumber(),
                        "general",
                        "Erro ao processar: " + e.getMessage(),
                        ""
                );
                errors.add(error);
                totalErrors++;
            }
        }

        InvoiceImportResultDTO result = new InvoiceImportResultDTO(
                totalProcessed,
                totalCreated,
                totalSkipped,
                totalErrors,
                duplicatesFound,
                errors,
                createdInvoices
        );

        logger.info("Importação concluída: {} processados, {} criados, {} ignorados, {} erros",
                totalProcessed, totalCreated, totalSkipped, totalErrors);

        return result;
    }
}
