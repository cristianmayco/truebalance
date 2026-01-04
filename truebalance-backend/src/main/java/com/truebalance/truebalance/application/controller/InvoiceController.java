package com.truebalance.truebalance.application.controller;

import com.truebalance.truebalance.application.dto.input.PartialPaymentRequestDTO;
import com.truebalance.truebalance.application.dto.input.UpdateInvoiceTotalAmountRequestDTO;
import com.truebalance.truebalance.application.dto.input.UpdateInvoiceRegisteredLimitRequestDTO;
import com.truebalance.truebalance.application.dto.output.InstallmentResponseDTO;
import com.truebalance.truebalance.application.dto.output.InvoiceBalanceDTO;
import com.truebalance.truebalance.application.dto.output.InvoiceResponseDTO;
import com.truebalance.truebalance.application.dto.output.PartialPaymentResponseDTO;
import com.truebalance.truebalance.domain.entity.Installment;
import com.truebalance.truebalance.domain.entity.Invoice;
import com.truebalance.truebalance.domain.entity.PartialPayment;
import com.truebalance.truebalance.domain.usecase.AutoCloseInvoicesIfNeeded;
import com.truebalance.truebalance.domain.usecase.CloseInvoice;
import com.truebalance.truebalance.domain.usecase.DeletePartialPayment;
import com.truebalance.truebalance.domain.usecase.GetInvoiceBalance;
import com.truebalance.truebalance.domain.usecase.GetInvoiceById;
import com.truebalance.truebalance.domain.usecase.GetInvoiceInstallments;
import com.truebalance.truebalance.domain.usecase.GetInvoicesByCreditCard;
import com.truebalance.truebalance.domain.usecase.GetPartialPaymentsByInvoice;
import com.truebalance.truebalance.domain.usecase.MarkInvoiceAsPaid;
import com.truebalance.truebalance.domain.usecase.MarkInvoiceAsUnpaid;
import com.truebalance.truebalance.domain.usecase.UpdateInvoiceUseAbsoluteValue;
import com.truebalance.truebalance.domain.usecase.UpdateInvoiceTotalAmount;
import com.truebalance.truebalance.domain.usecase.UpdateInvoiceRegisteredLimit;
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
    private final AutoCloseInvoicesIfNeeded autoCloseInvoicesIfNeeded;
    private final GetInvoiceBalance getInvoiceBalance;
    private final GetInvoiceInstallments getInvoiceInstallments;
    private final GetPartialPaymentsByInvoice getPartialPaymentsByInvoice;
    private final RegisterPartialPayment registerPartialPayment;
    private final DeletePartialPayment deletePartialPayment;
    private final MarkInvoiceAsPaid markInvoiceAsPaid;
    private final MarkInvoiceAsUnpaid markInvoiceAsUnpaid;
    private final UpdateInvoiceUseAbsoluteValue updateInvoiceUseAbsoluteValue;
    private final UpdateInvoiceTotalAmount updateInvoiceTotalAmount;
    private final UpdateInvoiceRegisteredLimit updateInvoiceRegisteredLimit;

    public InvoiceController(GetInvoiceById getInvoiceById,
                             GetInvoicesByCreditCard getInvoicesByCreditCard,
                             CloseInvoice closeInvoice,
                             AutoCloseInvoicesIfNeeded autoCloseInvoicesIfNeeded,
                             GetInvoiceBalance getInvoiceBalance,
                             GetInvoiceInstallments getInvoiceInstallments,
                             GetPartialPaymentsByInvoice getPartialPaymentsByInvoice,
                             RegisterPartialPayment registerPartialPayment,
                             DeletePartialPayment deletePartialPayment,
                             MarkInvoiceAsPaid markInvoiceAsPaid,
                             MarkInvoiceAsUnpaid markInvoiceAsUnpaid,
                             UpdateInvoiceUseAbsoluteValue updateInvoiceUseAbsoluteValue,
                             UpdateInvoiceTotalAmount updateInvoiceTotalAmount,
                             UpdateInvoiceRegisteredLimit updateInvoiceRegisteredLimit) {
        this.getInvoiceById = getInvoiceById;
        this.getInvoicesByCreditCard = getInvoicesByCreditCard;
        this.closeInvoice = closeInvoice;
        this.autoCloseInvoicesIfNeeded = autoCloseInvoicesIfNeeded;
        this.getInvoiceBalance = getInvoiceBalance;
        this.getInvoiceInstallments = getInvoiceInstallments;
        this.getPartialPaymentsByInvoice = getPartialPaymentsByInvoice;
        this.registerPartialPayment = registerPartialPayment;
        this.deletePartialPayment = deletePartialPayment;
        this.markInvoiceAsPaid = markInvoiceAsPaid;
        this.markInvoiceAsUnpaid = markInvoiceAsUnpaid;
        this.updateInvoiceUseAbsoluteValue = updateInvoiceUseAbsoluteValue;
        this.updateInvoiceTotalAmount = updateInvoiceTotalAmount;
        this.updateInvoiceRegisteredLimit = updateInvoiceRegisteredLimit;
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

        // Auto-close invoices if their closing date has passed
        logger.info("Chamando autoCloseInvoicesIfNeeded com {} faturas", invoices.size());
        autoCloseInvoicesIfNeeded.execute(invoices);
        logger.info("autoCloseInvoicesIfNeeded executado com sucesso");

        // Reload invoices to get updated status
        invoices = getInvoicesByCreditCard.execute(creditCardId);

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

        if (invoice.isPresent()) {
            // Auto-close invoice if its closing date has passed
            autoCloseInvoicesIfNeeded.execute(List.of(invoice.get()));

            // Reload invoice to get updated status
            invoice = getInvoiceById.execute(id);
        }

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
        logger.info("POST /invoices/{}/partial-payments - Registrando pagamento parcial de {}", id, requestDTO.getAmount());
        
        PartialPayment partialPayment = requestDTO.toPartialPayment();
        RegisterPartialPayment.RegisterPartialPaymentResult result = registerPartialPayment.execute(id, partialPayment);

        PartialPaymentResponseDTO response = PartialPaymentResponseDTO.fromPartialPaymentWithLimit(
                result.getPartialPayment(),
                result.getAvailableLimit()
        );

        logger.info("Pagamento parcial registrado com sucesso. ID={}, Limite disponível atualizado para {}", 
                result.getPartialPayment().getId(), result.getAvailableLimit());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
        logger.info("DELETE /partial-payments/{} - Deletando pagamento parcial", id);
        
        deletePartialPayment.execute(id);
        
        logger.info("Pagamento parcial ID={} deletado com sucesso", id);
        return ResponseEntity.noContent().build();
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

    @Operation(summary = "Atualizar flag de valor absoluto da fatura",
               description = "Define se a fatura deve usar valor absoluto (não recalcular pela soma das parcelas). " +
                           "Útil para faturas antigas onde não todas as contas foram cadastradas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Flag atualizada com sucesso",
                    content = @Content(mediaType = "application/json",
                                      schema = @Schema(implementation = InvoiceResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Fatura não encontrada", content = @Content)
    })
    @PatchMapping("/{id}/use-absolute-value")
    public ResponseEntity<InvoiceResponseDTO> updateUseAbsoluteValue(
            @Parameter(description = "ID da fatura", required = true)
            @PathVariable Long id,
            @Parameter(description = "Valor do flag (true = usar valor absoluto, false = calcular pela soma)", required = true)
            @RequestParam boolean useAbsoluteValue) {
        logger.info("PATCH /invoices/{}/use-absolute-value - Atualizando flag para {}", id, useAbsoluteValue);
        Optional<Invoice> invoice = updateInvoiceUseAbsoluteValue.execute(id, useAbsoluteValue);

        return invoice
                .map(inv -> {
                    logger.info("Flag useAbsoluteValue atualizado para {} na fatura ID={}", useAbsoluteValue, id);
                    return ResponseEntity.ok(InvoiceResponseDTO.fromInvoice(inv));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Atualizar valor total da fatura",
               description = "Atualiza o valor total de uma fatura. " +
                           "Apenas permitido quando useAbsoluteValue = true. " +
                           "Quando useAbsoluteValue = false, o total é calculado automaticamente pela soma das parcelas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Valor total atualizado com sucesso",
                    content = @Content(mediaType = "application/json",
                                      schema = @Schema(implementation = InvoiceResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Fatura não encontrada", content = @Content),
            @ApiResponse(responseCode = "400", description = "useAbsoluteValue não está habilitado para esta fatura", content = @Content)
    })
    @PatchMapping("/{id}/total-amount")
    public ResponseEntity<?> updateTotalAmount(
            @Parameter(description = "ID da fatura", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UpdateInvoiceTotalAmountRequestDTO requestDTO) {
        logger.info("PATCH /invoices/{}/total-amount - Atualizando valor total para {}", id, requestDTO.getTotalAmount());

        try {
            Optional<Invoice> invoice = updateInvoiceTotalAmount.execute(id, requestDTO.getTotalAmount());

            return invoice
                    .map(inv -> {
                        logger.info("Valor total atualizado para {} na fatura ID={}", requestDTO.getTotalAmount(), id);
                        return ResponseEntity.ok(InvoiceResponseDTO.fromInvoice(inv));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalStateException e) {
            logger.error("Erro ao atualizar valor total da fatura ID={}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Cadastrar limite disponível",
               description = "Define um limite disponível registrado para uma fatura fechada. " +
                           "Quando ativado, esta fatura se torna o ponto de partida para cálculos de limite, " +
                           "ignorando todas as faturas anteriores. Apenas permitido em faturas fechadas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Limite registrado atualizado com sucesso",
                    content = @Content(mediaType = "application/json",
                                      schema = @Schema(implementation = InvoiceResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Fatura não encontrada", content = @Content),
            @ApiResponse(responseCode = "400", description = "Fatura não está fechada ou dados inválidos", content = @Content)
    })
    @PatchMapping("/{id}/registered-limit")
    public ResponseEntity<?> updateRegisteredLimit(
            @Parameter(description = "ID da fatura", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UpdateInvoiceRegisteredLimitRequestDTO requestDTO) {
        logger.info("PATCH /invoices/{}/registered-limit - Atualizando limite registrado: register={}, limit={}",
                id, requestDTO.getRegisterAvailableLimit(), requestDTO.getRegisteredAvailableLimit());

        try {
            Optional<Invoice> invoice = updateInvoiceRegisteredLimit.execute(
                    id,
                    requestDTO.getRegisterAvailableLimit(),
                    requestDTO.getRegisteredAvailableLimit()
            );

            return invoice
                    .map(inv -> {
                        logger.info("Limite registrado atualizado na fatura ID={}", id);
                        return ResponseEntity.ok(InvoiceResponseDTO.fromInvoice(inv));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalStateException | IllegalArgumentException e) {
            logger.error("Erro ao atualizar limite registrado da fatura ID={}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
