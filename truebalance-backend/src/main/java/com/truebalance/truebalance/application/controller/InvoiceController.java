package com.truebalance.truebalance.application.controller;

import com.truebalance.truebalance.application.dto.input.InvoiceBulkImportRequestDTO;
import com.truebalance.truebalance.application.dto.input.PartialPaymentRequestDTO;
import com.truebalance.truebalance.application.dto.output.InstallmentResponseDTO;
import com.truebalance.truebalance.application.dto.output.InvoiceBalanceDTO;
import com.truebalance.truebalance.application.dto.output.InvoiceImportResultDTO;
import com.truebalance.truebalance.application.dto.output.InvoiceResponseDTO;
import com.truebalance.truebalance.application.dto.output.PartialPaymentResponseDTO;
import com.truebalance.truebalance.domain.entity.Installment;
import com.truebalance.truebalance.domain.entity.Invoice;
import com.truebalance.truebalance.domain.entity.PartialPayment;
import com.truebalance.truebalance.domain.usecase.CloseInvoice;
import com.truebalance.truebalance.domain.usecase.DeletePartialPayment;
import com.truebalance.truebalance.domain.usecase.GetInvoiceBalance;
import com.truebalance.truebalance.domain.usecase.GetInvoiceById;
import com.truebalance.truebalance.domain.usecase.GetInvoiceInstallments;
import com.truebalance.truebalance.domain.usecase.GetInvoicesByCreditCard;
import com.truebalance.truebalance.domain.usecase.GetPartialPaymentsByInvoice;
import com.truebalance.truebalance.domain.service.FileImportService;
import com.truebalance.truebalance.domain.usecase.ImportInvoicesInBulk;
import com.truebalance.truebalance.domain.usecase.MarkInvoiceAsPaid;
import com.truebalance.truebalance.domain.usecase.MarkInvoiceAsUnpaid;
import com.truebalance.truebalance.domain.usecase.RegisterPartialPayment;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/invoices")
@Tag(name = "Invoices", description = "API para gerenciamento de faturas de cartão de crédito")
public class InvoiceController {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceController.class);

    private final GetInvoiceById getInvoiceById;
    private final GetInvoicesByCreditCard getInvoicesByCreditCard;
    private final CloseInvoice closeInvoice;
    private final GetInvoiceBalance getInvoiceBalance;
    private final GetInvoiceInstallments getInvoiceInstallments;
    private final GetPartialPaymentsByInvoice getPartialPaymentsByInvoice;
    private final RegisterPartialPayment registerPartialPayment;
    private final DeletePartialPayment deletePartialPayment;
    private final MarkInvoiceAsPaid markInvoiceAsPaid;
    private final MarkInvoiceAsUnpaid markInvoiceAsUnpaid;
    private final ImportInvoicesInBulk importInvoicesInBulk;
    private final FileImportService fileImportService;

    public InvoiceController(GetInvoiceById getInvoiceById,
                             GetInvoicesByCreditCard getInvoicesByCreditCard,
                             CloseInvoice closeInvoice,
                             GetInvoiceBalance getInvoiceBalance,
                             GetInvoiceInstallments getInvoiceInstallments,
                             GetPartialPaymentsByInvoice getPartialPaymentsByInvoice,
                             RegisterPartialPayment registerPartialPayment,
                             DeletePartialPayment deletePartialPayment,
                             MarkInvoiceAsPaid markInvoiceAsPaid,
                             MarkInvoiceAsUnpaid markInvoiceAsUnpaid,
                             ImportInvoicesInBulk importInvoicesInBulk,
                             FileImportService fileImportService) {
        this.getInvoiceById = getInvoiceById;
        this.getInvoicesByCreditCard = getInvoicesByCreditCard;
        this.closeInvoice = closeInvoice;
        this.getInvoiceBalance = getInvoiceBalance;
        this.getInvoiceInstallments = getInvoiceInstallments;
        this.getPartialPaymentsByInvoice = getPartialPaymentsByInvoice;
        this.registerPartialPayment = registerPartialPayment;
        this.deletePartialPayment = deletePartialPayment;
        this.markInvoiceAsPaid = markInvoiceAsPaid;
        this.markInvoiceAsUnpaid = markInvoiceAsUnpaid;
        this.importInvoicesInBulk = importInvoicesInBulk;
        this.fileImportService = fileImportService;
    }

    @Operation(summary = "Listar faturas por cartão de crédito",
               description = "Retorna todas as faturas de um cartão de crédito específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de faturas retornada com sucesso",
                    content = @Content(mediaType = "application/json",
                                      schema = @Schema(implementation = InvoiceResponseDTO.class)))
    })
    @GetMapping
    public ResponseEntity<List<InvoiceResponseDTO>> getInvoicesByCreditCard(
            @Parameter(description = "ID do cartão de crédito", required = true)
            @RequestParam Long creditCardId) {
        logger.info("GET /invoices?creditCardId={} - Buscando faturas do cartão", creditCardId);
        List<Invoice> invoices = getInvoicesByCreditCard.execute(creditCardId);
        logger.info("Encontradas {} faturas para o cartão ID={}", invoices.size(), creditCardId);
        List<InvoiceResponseDTO> response = invoices.stream()
                .map(InvoiceResponseDTO::fromInvoice)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar fatura por ID",
               description = "Retorna os detalhes de uma fatura específica.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fatura encontrada com sucesso",
                    content = @Content(mediaType = "application/json",
                                      schema = @Schema(implementation = InvoiceResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Fatura não encontrada", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponseDTO> getInvoiceById(
            @Parameter(description = "ID da fatura a ser buscada", required = true)
            @PathVariable Long id) {
        Optional<Invoice> invoice = getInvoiceById.execute(id);

        return invoice
                .map(inv -> ResponseEntity.ok(InvoiceResponseDTO.fromInvoice(inv)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Fechar fatura",
               description = "Fecha a fatura, calculando pagamentos parciais e transferindo saldo negativo se aplicável. " +
                             "BR-I-012: Considera pagamentos parciais. BR-I-016: Transfere saldo negativo para próxima fatura.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fatura fechada com sucesso",
                    content = @Content(mediaType = "application/json",
                                      schema = @Schema(implementation = InvoiceResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Fatura não encontrada", content = @Content),
            @ApiResponse(responseCode = "400", description = "Fatura já está fechada", content = @Content)
    })
    @PostMapping("/{id}/close")
    public ResponseEntity<InvoiceResponseDTO> closeInvoice(
            @Parameter(description = "ID da fatura a ser fechada", required = true)
            @PathVariable Long id) {
        try {
            Optional<Invoice> invoice = closeInvoice.execute(id);

            return invoice
                    .map(inv -> ResponseEntity.ok(InvoiceResponseDTO.fromInvoice(inv)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Buscar saldo atual da fatura",
               description = "Calcula o saldo atual da fatura considerando total, saldo anterior e pagamentos parciais. " +
                             "BR-I-011: currentBalance = totalAmount + previousBalance - partialPaymentsTotal")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saldo calculado com sucesso",
                    content = @Content(mediaType = "application/json",
                                      schema = @Schema(implementation = InvoiceBalanceDTO.class))),
            @ApiResponse(responseCode = "404", description = "Fatura não encontrada", content = @Content)
    })
    @GetMapping("/{id}/balance")
    public ResponseEntity<InvoiceBalanceDTO> getInvoiceBalance(
            @Parameter(description = "ID da fatura", required = true)
            @PathVariable Long id) {
        Optional<InvoiceBalanceDTO> balance = getInvoiceBalance.execute(id);

        return balance
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Listar parcelas da fatura",
               description = "Retorna todas as parcelas de uma fatura específica, ordenadas por data de vencimento.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parcelas retornadas com sucesso",
                    content = @Content(mediaType = "application/json",
                                      schema = @Schema(implementation = InstallmentResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Fatura não encontrada", content = @Content)
    })
    @GetMapping("/{id}/installments")
    public ResponseEntity<List<InstallmentResponseDTO>> getInvoiceInstallments(
            @Parameter(description = "ID da fatura", required = true)
            @PathVariable Long id) {
        // Verify invoice exists
        Optional<Invoice> invoice = getInvoiceById.execute(id);

        if (invoice.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Installment> installments = getInvoiceInstallments.execute(id);
        List<InstallmentResponseDTO> response = installments.stream()
                .map(InstallmentResponseDTO::fromInstallment)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Registrar pagamento parcial",
               description = "Registra um pagamento antecipado parcial em uma fatura aberta. " +
                             "BR-PP-001: Apenas em faturas abertas com allowsPartialPayment = true. " +
                             "BR-PP-002: Valor pode exceder saldo da fatura. " +
                             "BR-PP-006: Retorna limite disponível atualizado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pagamento registrado com sucesso",
                    content = @Content(mediaType = "application/json",
                                      schema = @Schema(implementation = PartialPaymentResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Fatura não encontrada", content = @Content),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou validação falhou", content = @Content)
    })
    @PostMapping("/{id}/partial-payments")
    public ResponseEntity<PartialPaymentResponseDTO> registerPartialPayment(
            @Parameter(description = "ID da fatura", required = true)
            @PathVariable Long id,
            @Valid @RequestBody PartialPaymentRequestDTO requestDTO) {
        try {
            PartialPayment partialPayment = requestDTO.toPartialPayment();
            RegisterPartialPayment.RegisterPartialPaymentResult result = registerPartialPayment.execute(id, partialPayment);

            PartialPaymentResponseDTO response = PartialPaymentResponseDTO.fromPartialPaymentWithLimit(
                    result.getPartialPayment(),
                    result.getAvailableLimit()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Listar pagamentos parciais da fatura",
               description = "Retorna todos os pagamentos parciais de uma fatura, ordenados por data (mais recente primeiro).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagamentos retornados com sucesso",
                    content = @Content(mediaType = "application/json",
                                      schema = @Schema(implementation = PartialPaymentResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Fatura não encontrada", content = @Content)
    })
    @GetMapping("/{id}/partial-payments")
    public ResponseEntity<List<PartialPaymentResponseDTO>> getPartialPaymentsByInvoice(
            @Parameter(description = "ID da fatura", required = true)
            @PathVariable Long id) {
        // Verify invoice exists
        Optional<Invoice> invoice = getInvoiceById.execute(id);

        if (invoice.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<PartialPayment> partialPayments = getPartialPaymentsByInvoice.execute(id);
        List<PartialPaymentResponseDTO> response = partialPayments.stream()
                .map(PartialPaymentResponseDTO::fromPartialPayment)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Deletar pagamento parcial",
               description = "Remove um pagamento parcial de uma fatura aberta. " +
                             "BR-PP-003: Apenas permitido em faturas abertas. " +
                             "BR-PP-004: Pagamentos não podem ser editados, apenas deletados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pagamento deletado com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Pagamento não encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Fatura está fechada", content = @Content)
    })
    @DeleteMapping("/partial-payments/{id}")
    public ResponseEntity<Void> deletePartialPayment(
            @Parameter(description = "ID do pagamento parcial a ser deletado", required = true)
            @PathVariable Long id) {
        try {
            boolean deleted = deletePartialPayment.execute(id);

            return deleted
                    ? ResponseEntity.noContent().build()
                    : ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Marcar fatura como paga",
               description = "Marca uma fatura como paga.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fatura marcada como paga com sucesso",
                    content = @Content(mediaType = "application/json",
                                      schema = @Schema(implementation = InvoiceResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Fatura não encontrada", content = @Content)
    })
    @PatchMapping("/{id}/mark-as-paid")
    public ResponseEntity<InvoiceResponseDTO> markInvoiceAsPaid(
            @Parameter(description = "ID da fatura a ser marcada como paga", required = true)
            @PathVariable Long id) {
        logger.info("PATCH /invoices/{}/mark-as-paid - Marcando fatura como paga", id);
        Optional<Invoice> invoice = markInvoiceAsPaid.execute(id);

        return invoice
                .map(inv -> {
                    logger.info("Fatura ID={} marcada como paga", id);
                    return ResponseEntity.ok(InvoiceResponseDTO.fromInvoice(inv));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Marcar fatura como não paga",
               description = "Marca uma fatura como não paga.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fatura marcada como não paga com sucesso",
                    content = @Content(mediaType = "application/json",
                                      schema = @Schema(implementation = InvoiceResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Fatura não encontrada", content = @Content)
    })
    @PatchMapping("/{id}/mark-as-unpaid")
    public ResponseEntity<InvoiceResponseDTO> markInvoiceAsUnpaid(
            @Parameter(description = "ID da fatura a ser marcada como não paga", required = true)
            @PathVariable Long id) {
        logger.info("PATCH /invoices/{}/mark-as-unpaid - Marcando fatura como não paga", id);
        Optional<Invoice> invoice = markInvoiceAsUnpaid.execute(id);

        return invoice
                .map(inv -> {
                    logger.info("Fatura ID={} marcada como não paga", id);
                    return ResponseEntity.ok(InvoiceResponseDTO.fromInvoice(inv));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Importar faturas em massa",
               description = "Importa múltiplas faturas a partir de arquivo CSV/XLS. " +
                             "Permite escolher estratégia para duplicatas: ignorar (SKIP) ou criar duplicadas (CREATE_DUPLICATE). " +
                             "Critério de duplicata: mesmo cartão de crédito + mesmo mês de referência.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Importação processada com sucesso",
                    content = @Content(mediaType = "application/json",
                                      schema = @Schema(implementation = InvoiceImportResultDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    })
    @PostMapping("/bulk-import")
    public ResponseEntity<InvoiceImportResultDTO> bulkImport(
            @Valid @RequestBody InvoiceBulkImportRequestDTO request) {

        logger.info("POST /invoices/bulk-import - Importando {} itens com estratégia {}",
                request.getItems().size(), request.getDuplicateStrategy());

        InvoiceImportResultDTO result = importInvoicesInBulk.execute(request);

        logger.info("Importação concluída: {} criadas, {} ignoradas, {} erros",
                result.getTotalCreated(), result.getTotalSkipped(), result.getTotalErrors());

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Importar faturas de arquivo CSV/XLS",
               description = "Importa faturas em massa a partir de um arquivo CSV ou XLS/XLSX. " +
                             "O arquivo deve conter cabeçalhos: ID Cartão, Mês de Referência, Valor Total. " +
                             "Opcionalmente: Saldo Anterior, Fechada, Paga.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Importação concluída com sucesso",
                    content = @Content(mediaType = "application/json",
                                      schema = @Schema(implementation = InvoiceImportResultDTO.class))),
            @ApiResponse(responseCode = "400", description = "Arquivo inválido ou erro no processamento", content = @Content)
    })
    @PostMapping(value = "/bulk-import-file", consumes = "multipart/form-data")
    public ResponseEntity<InvoiceImportResultDTO> bulkImportFromFile(
            @Parameter(description = "Arquivo CSV ou XLS/XLSX para importação", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Estratégia para duplicatas: SKIP ou CREATE_DUPLICATE", required = true)
            @RequestParam("duplicateStrategy") InvoiceBulkImportRequestDTO.DuplicateStrategy duplicateStrategy) {

        logger.info("POST /invoices/bulk-import-file - Importando arquivo: {} com estratégia {}",
                file.getOriginalFilename(), duplicateStrategy);

        try {
            // Parse file
            List<com.truebalance.truebalance.application.dto.input.InvoiceImportItemDTO> items =
                    fileImportService.parseInvoicesFromFile(file);

            // Create request
            InvoiceBulkImportRequestDTO request = new InvoiceBulkImportRequestDTO();
            request.setItems(items);
            request.setDuplicateStrategy(duplicateStrategy);

            // Execute import
            InvoiceImportResultDTO result = importInvoicesInBulk.execute(request);

            logger.info("Importação de arquivo concluída: {} criadas, {} ignoradas, {} erros",
                    result.getTotalCreated(), result.getTotalSkipped(), result.getTotalErrors());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Erro ao importar arquivo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

}
