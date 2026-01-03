package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.application.dto.input.BillBulkImportRequestDTO;
import com.truebalance.truebalance.application.dto.input.BillImportItemDTO;
import com.truebalance.truebalance.application.dto.output.BillImportResultDTO;
import com.truebalance.truebalance.application.dto.output.BillResponseDTO;
import com.truebalance.truebalance.application.dto.output.DuplicateInfoDTO;
import com.truebalance.truebalance.application.dto.output.ImportErrorDTO;
import com.truebalance.truebalance.domain.entity.Bill;
import com.truebalance.truebalance.domain.port.BillRepositoryPort;
import com.truebalance.truebalance.infra.db.entity.BillEntity;
import com.truebalance.truebalance.infra.db.repository.BillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ImportBillsInBulk {

    private static final Logger logger = LoggerFactory.getLogger(ImportBillsInBulk.class);

    private final CreateBill createBill;
    private final CreateBillWithCreditCard createBillWithCreditCard;
    private final BillRepository billRepository;

    public ImportBillsInBulk(CreateBill createBill,
                             CreateBillWithCreditCard createBillWithCreditCard,
                             BillRepository billRepository) {
        this.createBill = createBill;
        this.createBillWithCreditCard = createBillWithCreditCard;
        this.billRepository = billRepository;
    }

    public BillImportResultDTO execute(BillBulkImportRequestDTO request) {
        logger.info("Iniciando importação em massa de {} contas com estratégia: {}",
                request.getItems().size(), request.getDuplicateStrategy());

        List<DuplicateInfoDTO> duplicatesFound = new ArrayList<>();
        List<ImportErrorDTO> errors = new ArrayList<>();
        List<BillResponseDTO> createdBills = new ArrayList<>();

        int totalProcessed = request.getItems().size();
        int totalCreated = 0;
        int totalSkipped = 0;
        int totalErrors = 0;

        for (BillImportItemDTO item : request.getItems()) {
            try {
                logger.debug("Processando item da linha {}: {}", item.getLineNumber(), item.getName());

                // Verificar duplicata
                Optional<BillEntity> duplicateEntity = billRepository.findDuplicate(
                        item.getName(),
                        item.getTotalAmount(),
                        item.getExecutionDate(),
                        item.getNumberOfInstallments()
                );

                if (duplicateEntity.isPresent()) {
                    logger.debug("Duplicata encontrada para linha {}: Bill ID={}",
                            item.getLineNumber(), duplicateEntity.get().getId());

                    if (request.getDuplicateStrategy() == BillBulkImportRequestDTO.DuplicateStrategy.SKIP) {
                        // Adicionar em duplicatas e pular
                        DuplicateInfoDTO duplicateInfo = new DuplicateInfoDTO(
                                item.getLineNumber(),
                                item.getName(),
                                item.getTotalAmount(),
                                item.getExecutionDate(),
                                item.getNumberOfInstallments(),
                                duplicateEntity.get().getId(),
                                String.format("Duplicata encontrada: registro existente com mesmo nome, " +
                                        "valor, data e número de parcelas (ID: %d)", duplicateEntity.get().getId())
                        );
                        duplicatesFound.add(duplicateInfo);
                        totalSkipped++;
                        logger.debug("Conta da linha {} ignorada (duplicata)", item.getLineNumber());
                        continue;
                    } else {
                        logger.debug("Criando duplicata para linha {} conforme estratégia CREATE_DUPLICATE",
                                item.getLineNumber());
                    }
                }

                // Criar bill
                Bill bill = item.toBill();
                Bill createdBill;

                if (item.getCreditCardId() != null) {
                    logger.debug("Criando conta vinculada ao cartão de crédito ID={}", item.getCreditCardId());
                    createdBill = createBillWithCreditCard.execute(bill, item.getCreditCardId());
                } else {
                    logger.debug("Criando conta standalone");
                    createdBill = createBill.addBill(bill);
                }

                createdBills.add(BillResponseDTO.fromBill(createdBill, item.getCreditCardId()));
                totalCreated++;
                logger.debug("Conta da linha {} criada com sucesso! ID={}",
                        item.getLineNumber(), createdBill.getId());

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

        BillImportResultDTO result = new BillImportResultDTO(
                totalProcessed,
                totalCreated,
                totalSkipped,
                totalErrors,
                duplicatesFound,
                errors,
                createdBills
        );

        logger.info("Importação concluída: {} processados, {} criados, {} ignorados, {} erros",
                totalProcessed, totalCreated, totalSkipped, totalErrors);

        return result;
    }
}
