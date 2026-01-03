package com.truebalance.truebalance.application.controller;

import com.truebalance.truebalance.application.dto.input.BillBulkImportRequestDTO;
import com.truebalance.truebalance.application.dto.input.CreditCardBulkImportRequestDTO;
import com.truebalance.truebalance.application.dto.input.InvoiceBulkImportRequestDTO;
import com.truebalance.truebalance.application.dto.output.BillImportResultDTO;
import com.truebalance.truebalance.application.dto.output.CreditCardImportResultDTO;
import com.truebalance.truebalance.application.dto.output.InvoiceImportResultDTO;
import com.truebalance.truebalance.domain.entity.Bill;
import com.truebalance.truebalance.domain.entity.Invoice;
import com.truebalance.truebalance.domain.port.BillRepositoryPort;
import com.truebalance.truebalance.domain.port.CreditCardRepositoryPort;
import com.truebalance.truebalance.domain.port.InvoiceRepositoryPort;
import com.truebalance.truebalance.domain.service.UnifiedExportService;
import com.truebalance.truebalance.infra.db.repository.BillRepository;
import com.truebalance.truebalance.infra.db.repository.InvoiceRepository;
import com.truebalance.truebalance.domain.service.UnifiedImportService;
import com.truebalance.truebalance.domain.usecase.ImportBillsInBulk;
import com.truebalance.truebalance.domain.usecase.ImportCreditCardsInBulk;
import com.truebalance.truebalance.domain.usecase.ImportInvoicesInBulk;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/unified")
@Tag(name = "Unified Import/Export", description = "API para importação e exportação unificada de todas as entidades")
public class UnifiedImportExportController {

    private static final Logger logger = LoggerFactory.getLogger(UnifiedImportExportController.class);

    private final UnifiedExportService unifiedExportService;
    private final UnifiedImportService unifiedImportService;
    private final BillRepositoryPort billRepositoryPort;
    private final BillRepository billRepository;
    private final CreditCardRepositoryPort creditCardRepositoryPort;
    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final InvoiceRepository invoiceRepository;
    private final ImportBillsInBulk importBillsInBulk;
    private final ImportCreditCardsInBulk importCreditCardsInBulk;
    private final ImportInvoicesInBulk importInvoicesInBulk;

    public UnifiedImportExportController(
            UnifiedExportService unifiedExportService,
            UnifiedImportService unifiedImportService,
            BillRepositoryPort billRepositoryPort,
            BillRepository billRepository,
            CreditCardRepositoryPort creditCardRepositoryPort,
            InvoiceRepositoryPort invoiceRepositoryPort,
            InvoiceRepository invoiceRepository,
            ImportBillsInBulk importBillsInBulk,
            ImportCreditCardsInBulk importCreditCardsInBulk,
            ImportInvoicesInBulk importInvoicesInBulk) {
        this.unifiedExportService = unifiedExportService;
        this.unifiedImportService = unifiedImportService;
        this.billRepositoryPort = billRepositoryPort;
        this.billRepository = billRepository;
        this.creditCardRepositoryPort = creditCardRepositoryPort;
        this.invoiceRepositoryPort = invoiceRepositoryPort;
        this.invoiceRepository = invoiceRepository;
        this.importBillsInBulk = importBillsInBulk;
        this.importCreditCardsInBulk = importCreditCardsInBulk;
        this.importInvoicesInBulk = importInvoicesInBulk;
    }

    @Operation(summary = "Exportar todas as entidades para Excel",
               description = "Exporta todas as contas, cartões de crédito e faturas para um único arquivo Excel com múltiplas abas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exportação realizada com sucesso",
                    content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
    })
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportAll() {
        logger.info("GET /unified/export - Exportando todas as entidades");

        try {
            // Buscar todas as entidades
            List<com.truebalance.truebalance.application.dto.output.BillResponseDTO> bills = 
                    billRepository.findAll().stream()
                    .map(entity -> {
                        Bill bill = new Bill();
                        bill.setId(entity.getId());
                        bill.setName(entity.getName());
                        bill.setExecutionDate(entity.getExecutionDate());
                        bill.setTotalAmount(entity.getTotalAmount());
                        bill.setNumberOfInstallments(entity.getNumberOfInstallments());
                        bill.setInstallmentAmount(entity.getInstallmentAmount());
                        bill.setDescription(entity.getDescription());
                        bill.setIsRecurring(entity.getIsRecurring());
                        bill.setCreatedAt(entity.getCreatedAt());
                        bill.setUpdatedAt(entity.getUpdatedAt());
                        // creditCardId será null pois não está diretamente na BillEntity
                        return com.truebalance.truebalance.application.dto.output.BillResponseDTO.fromBill(bill, null);
                    })
                    .collect(java.util.stream.Collectors.toList());

            List<com.truebalance.truebalance.application.dto.output.CreditCardResponseDTO> creditCards = 
                    creditCardRepositoryPort.findAll().stream()
                    .map(com.truebalance.truebalance.application.dto.output.CreditCardResponseDTO::fromCreditCard)
                    .collect(java.util.stream.Collectors.toList());

            List<com.truebalance.truebalance.application.dto.output.InvoiceResponseDTO> invoices = 
                    invoiceRepository.findAll().stream()
                    .map(entity -> {
                        Invoice invoice = new Invoice();
                        invoice.setId(entity.getId());
                        invoice.setCreditCardId(entity.getCreditCardId());
                        invoice.setReferenceMonth(entity.getReferenceMonth());
                        invoice.setTotalAmount(entity.getTotalAmount());
                        invoice.setPreviousBalance(entity.getPreviousBalance());
                        invoice.setClosed(entity.isClosed());
                        invoice.setPaid(entity.isPaid());
                        invoice.setCreatedAt(entity.getCreatedAt());
                        invoice.setUpdatedAt(entity.getUpdatedAt());
                        return com.truebalance.truebalance.application.dto.output.InvoiceResponseDTO.fromInvoice(invoice);
                    })
                    .collect(java.util.stream.Collectors.toList());

            // Gerar arquivo Excel
            byte[] excelData = unifiedExportService.exportToExcel(bills, creditCards, invoices);

            // Criar nome do arquivo com timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "truebalance_export_" + timestamp + ".xlsx";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(excelData.length);

            logger.info("Exportação concluída: {} contas, {} cartões, {} faturas", 
                    bills.size(), creditCards.size(), invoices.size());

            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Erro ao exportar: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Importar todas as entidades de um arquivo Excel",
               description = "Importa contas, cartões de crédito e faturas de um único arquivo Excel com múltiplas abas. " +
                           "O arquivo deve conter as abas: 'Contas', 'Cartões de Crédito' e 'Faturas'.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Importação processada com sucesso",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Arquivo inválido ou erro no processamento", content = @Content)
    })
    @PostMapping(value = "/import", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> importAll(
            @Parameter(description = "Arquivo Excel (XLS ou XLSX) para importação", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Estratégia para duplicatas: SKIP ou CREATE_DUPLICATE", required = true)
            @RequestParam("duplicateStrategy") String duplicateStrategy) {

        logger.info("POST /unified/import - Importando arquivo: {} com estratégia {}",
                file.getOriginalFilename(), duplicateStrategy);

        Map<String, Object> result = new HashMap<>();

        try {
            // Parse do arquivo
            UnifiedImportService.UnifiedImportResult parsedData = unifiedImportService.parseUnifiedFile(file);

            // Processar cada tipo de entidade
            BillImportResultDTO billsResult = null;
            CreditCardImportResultDTO creditCardsResult = null;
            InvoiceImportResultDTO invoicesResult = null;

            // Importar Contas
            if (!parsedData.getBills().isEmpty()) {
                BillBulkImportRequestDTO.DuplicateStrategy billsStrategy = 
                        duplicateStrategy.equals("SKIP") 
                                ? BillBulkImportRequestDTO.DuplicateStrategy.SKIP 
                                : BillBulkImportRequestDTO.DuplicateStrategy.CREATE_DUPLICATE;
                
                BillBulkImportRequestDTO billsRequest = new BillBulkImportRequestDTO();
                billsRequest.setItems(parsedData.getBills());
                billsRequest.setDuplicateStrategy(billsStrategy);
                
                billsResult = importBillsInBulk.execute(billsRequest);
                logger.info("Importação de contas concluída: {} criadas, {} ignoradas, {} erros",
                        billsResult.getTotalCreated(), billsResult.getTotalSkipped(), billsResult.getTotalErrors());
            }

            // Importar Cartões de Crédito
            if (!parsedData.getCreditCards().isEmpty()) {
                CreditCardBulkImportRequestDTO.DuplicateStrategy creditCardsStrategy = 
                        duplicateStrategy.equals("SKIP") 
                                ? CreditCardBulkImportRequestDTO.DuplicateStrategy.SKIP 
                                : CreditCardBulkImportRequestDTO.DuplicateStrategy.CREATE_DUPLICATE;
                
                CreditCardBulkImportRequestDTO creditCardsRequest = new CreditCardBulkImportRequestDTO();
                creditCardsRequest.setItems(parsedData.getCreditCards());
                creditCardsRequest.setDuplicateStrategy(creditCardsStrategy);
                
                creditCardsResult = importCreditCardsInBulk.execute(creditCardsRequest);
                logger.info("Importação de cartões concluída: {} criados, {} ignorados, {} erros",
                        creditCardsResult.getTotalCreated(), creditCardsResult.getTotalSkipped(), creditCardsResult.getTotalErrors());
            }

            // Importar Faturas
            if (!parsedData.getInvoices().isEmpty()) {
                InvoiceBulkImportRequestDTO.DuplicateStrategy invoicesStrategy = 
                        duplicateStrategy.equals("SKIP") 
                                ? InvoiceBulkImportRequestDTO.DuplicateStrategy.SKIP 
                                : InvoiceBulkImportRequestDTO.DuplicateStrategy.CREATE_DUPLICATE;
                
                InvoiceBulkImportRequestDTO invoicesRequest = new InvoiceBulkImportRequestDTO();
                invoicesRequest.setItems(parsedData.getInvoices());
                invoicesRequest.setDuplicateStrategy(invoicesStrategy);
                
                invoicesResult = importInvoicesInBulk.execute(invoicesRequest);
                logger.info("Importação de faturas concluída: {} criadas, {} ignoradas, {} erros",
                        invoicesResult.getTotalCreated(), invoicesResult.getTotalSkipped(), invoicesResult.getTotalErrors());
            }

            // Montar resultado
            result.put("bills", billsResult);
            result.put("creditCards", creditCardsResult);
            result.put("invoices", invoicesResult);

            // Resumo geral
            int totalCreated = (billsResult != null ? billsResult.getTotalCreated() : 0) +
                              (creditCardsResult != null ? creditCardsResult.getTotalCreated() : 0) +
                              (invoicesResult != null ? invoicesResult.getTotalCreated() : 0);
            
            int totalSkipped = (billsResult != null ? billsResult.getTotalSkipped() : 0) +
                              (creditCardsResult != null ? creditCardsResult.getTotalSkipped() : 0) +
                              (invoicesResult != null ? invoicesResult.getTotalSkipped() : 0);
            
            int totalErrors = (billsResult != null ? billsResult.getTotalErrors() : 0) +
                             (creditCardsResult != null ? creditCardsResult.getTotalErrors() : 0) +
                             (invoicesResult != null ? invoicesResult.getTotalErrors() : 0);

            result.put("summary", Map.of(
                    "totalCreated", totalCreated,
                    "totalSkipped", totalSkipped,
                    "totalErrors", totalErrors
            ));

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Erro ao importar arquivo: {}", e.getMessage(), e);
            result.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
    }
}
