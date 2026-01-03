package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.application.dto.input.CreditCardBulkImportRequestDTO;
import com.truebalance.truebalance.application.dto.input.CreditCardImportItemDTO;
import com.truebalance.truebalance.application.dto.output.CreditCardDuplicateInfoDTO;
import com.truebalance.truebalance.application.dto.output.CreditCardImportResultDTO;
import com.truebalance.truebalance.application.dto.output.CreditCardResponseDTO;
import com.truebalance.truebalance.application.dto.output.ImportErrorDTO;
import com.truebalance.truebalance.domain.entity.CreditCard;
import com.truebalance.truebalance.domain.port.CreditCardRepositoryPort;
import com.truebalance.truebalance.infra.db.entity.CreditCardEntity;
import com.truebalance.truebalance.infra.db.repository.CreditCardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ImportCreditCardsInBulk {

    private static final Logger logger = LoggerFactory.getLogger(ImportCreditCardsInBulk.class);

    private final CreateCreditCard createCreditCard;
    private final CreditCardRepository creditCardRepository;

    public ImportCreditCardsInBulk(CreateCreditCard createCreditCard,
                                   CreditCardRepository creditCardRepository) {
        this.createCreditCard = createCreditCard;
        this.creditCardRepository = creditCardRepository;
    }

    public CreditCardImportResultDTO execute(CreditCardBulkImportRequestDTO request) {
        logger.info("Iniciando importação em massa de {} cartões de crédito com estratégia: {}",
                request.getItems().size(), request.getDuplicateStrategy());

        List<CreditCardDuplicateInfoDTO> duplicatesFound = new ArrayList<>();
        List<ImportErrorDTO> errors = new ArrayList<>();
        List<CreditCardResponseDTO> createdCreditCards = new ArrayList<>();

        int totalProcessed = request.getItems().size();
        int totalCreated = 0;
        int totalSkipped = 0;
        int totalErrors = 0;

        for (CreditCardImportItemDTO item : request.getItems()) {
            try {
                logger.debug("Processando item da linha {}: {}", item.getLineNumber(), item.getName());

                // Verificar duplicata por nome
                Optional<CreditCardEntity> duplicateEntity = creditCardRepository.findByName(item.getName());

                if (duplicateEntity.isPresent()) {
                    logger.debug("Duplicata encontrada para linha {}: CreditCard ID={}",
                            item.getLineNumber(), duplicateEntity.get().getId());

                    if (request.getDuplicateStrategy() == CreditCardBulkImportRequestDTO.DuplicateStrategy.SKIP) {
                        // Adicionar em duplicatas e pular
                        CreditCardDuplicateInfoDTO duplicateInfo = new CreditCardDuplicateInfoDTO(
                                item.getLineNumber(),
                                item.getName(),
                                item.getCreditLimit(),
                                item.getClosingDay(),
                                item.getDueDay(),
                                duplicateEntity.get().getId(),
                                String.format("Duplicata encontrada: já existe um cartão com o nome '%s' (ID: %d)",
                                        item.getName(), duplicateEntity.get().getId())
                        );
                        duplicatesFound.add(duplicateInfo);
                        totalSkipped++;
                        logger.debug("Cartão da linha {} ignorado (duplicata)", item.getLineNumber());
                        continue;
                    } else {
                        logger.debug("Criando duplicata para linha {} conforme estratégia CREATE_DUPLICATE",
                                item.getLineNumber());
                    }
                }

                // Criar credit card
                CreditCard creditCard = item.toCreditCard();
                CreditCard createdCreditCard = createCreditCard.execute(creditCard);

                createdCreditCards.add(CreditCardResponseDTO.fromCreditCard(createdCreditCard));
                totalCreated++;
                logger.debug("Cartão da linha {} criado com sucesso! ID={}",
                        item.getLineNumber(), createdCreditCard.getId());

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

        CreditCardImportResultDTO result = new CreditCardImportResultDTO(
                totalProcessed,
                totalCreated,
                totalSkipped,
                totalErrors,
                duplicatesFound,
                errors,
                createdCreditCards
        );

        logger.info("Importação concluída: {} processados, {} criados, {} ignorados, {} erros",
                totalProcessed, totalCreated, totalSkipped, totalErrors);

        return result;
    }
}
