package com.truebalance.truebalance.application.controller;

import com.truebalance.truebalance.application.dto.input.ImportDataDTO;
import com.truebalance.truebalance.application.dto.output.ExportDataDTO;
import com.truebalance.truebalance.application.dto.output.ImportResultDTO;
import com.truebalance.truebalance.application.dto.output.ProcessPostImportResultDTO;
import com.truebalance.truebalance.domain.usecase.ExportData;
import com.truebalance.truebalance.domain.usecase.ImportData;
import com.truebalance.truebalance.domain.usecase.ProcessPostImport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/import-export")
@Tag(name = "Import/Export", description = "API para importação e exportação de dados (contas, cartões, faturas)")
public class ImportExportController {

    private static final Logger logger = LoggerFactory.getLogger(ImportExportController.class);
    private final ExportData exportData;
    private final ImportData importData;
    private final ProcessPostImport processPostImport;

    public ImportExportController(ExportData exportData, ImportData importData, ProcessPostImport processPostImport) {
        this.exportData = exportData;
        this.importData = importData;
        this.processPostImport = processPostImport;
    }

    @Operation(summary = "Exportar todos os dados",
               description = "Exporta todos os registros do banco de dados (contas, cartões de crédito e faturas) em formato JSON.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados exportados com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExportDataDTO.class)))
    })
    @GetMapping("/export")
    public ResponseEntity<ExportDataDTO> exportData() {
        logger.info("GET /import-export/export - Exportando todos os dados");
        ExportDataDTO exportDataDTO = exportData.execute();
        logger.info("Exportação concluída: {} contas, {} cartões, {} faturas, {} categorias",
                exportDataDTO.getBills() != null ? exportDataDTO.getBills().size() : 0,
                exportDataDTO.getCreditCards() != null ? exportDataDTO.getCreditCards().size() : 0,
                exportDataDTO.getInvoices() != null ? exportDataDTO.getInvoices().size() : 0,
                exportDataDTO.getCategories() != null ? exportDataDTO.getCategories().size() : 0);
        return ResponseEntity.ok(exportDataDTO);
    }

    @Operation(summary = "Importar dados",
               description = "Importa dados (contas, cartões de crédito e faturas) a partir de um arquivo JSON. " +
                             "Duplicatas são ignoradas automaticamente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Importação processada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ImportResultDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    })
    @PostMapping("/import")
    public ResponseEntity<ImportResultDTO> importData(@Valid @RequestBody ImportDataDTO importDataDTO) {
        logger.info("POST /import-export/import - Importando dados");
        ImportResultDTO result = importData.execute(importDataDTO);
        logger.info("Importação concluída: {} processados, {} criados, {} ignorados, {} erros",
                result.getTotalProcessed(), result.getTotalCreated(), result.getTotalSkipped(), result.getTotalErrors());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Processar dados pós-importação",
               description = "Processa contas e cartões após importação para corrigir vínculos e recalcular faturas. " +
                           "Recalcula os totais das faturas baseado nas parcelas existentes.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Processamento concluído com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProcessPostImportResultDTO.class)))
    })
    @PostMapping("/process")
    public ResponseEntity<ProcessPostImportResultDTO> processPostImport() {
        logger.info("POST /import-export/process - Processando dados pós-importação");
        ProcessPostImport.ProcessPostImportResult result = processPostImport.execute();
        
        ProcessPostImportResultDTO dto = new ProcessPostImportResultDTO(
                result.getInvoicesRecalculated(),
                result.getBillsProcessed(),
                result.getErrors(),
                result.getErrorMessages()
        );
        
        logger.info("Processamento pós-importação concluído: {} faturas recalculadas, {} contas processadas, {} erros",
                dto.getInvoicesRecalculated(), dto.getBillsProcessed(), dto.getErrors());
        return ResponseEntity.ok(dto);
    }
}
