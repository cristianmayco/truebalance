package com.truebalance.truebalance.application.controller;

import com.truebalance.truebalance.application.dto.input.CreditCardRequestDTO;
import com.truebalance.truebalance.application.dto.output.AvailableLimitDTO;
import com.truebalance.truebalance.application.dto.output.CreditCardResponseDTO;
import com.truebalance.truebalance.application.dto.output.InvoiceResponseDTO;
import com.truebalance.truebalance.domain.entity.CreditCard;
import com.truebalance.truebalance.domain.entity.Invoice;
import com.truebalance.truebalance.domain.usecase.*;
import com.truebalance.truebalance.domain.usecase.AvailableLimitResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/credit-cards")
@Tag(name = "Credit Cards", description = "API para gerenciamento de cartões de crédito")
public class CreditCardController {

    private final CreateCreditCard createCreditCard;
    private final GetAllCreditCards getAllCreditCards;
    private final GetCreditCardById getCreditCardById;
    private final UpdateCreditCard updateCreditCard;
    private final DeleteCreditCard deleteCreditCard;
    private final GetInvoicesByCreditCard getInvoicesByCreditCard;
    private final GetAvailableLimit getAvailableLimit;

    public CreditCardController(CreateCreditCard createCreditCard,
                                 GetAllCreditCards getAllCreditCards,
                                 GetCreditCardById getCreditCardById,
                                 UpdateCreditCard updateCreditCard,
                                 DeleteCreditCard deleteCreditCard,
                                 GetInvoicesByCreditCard getInvoicesByCreditCard,
                                 GetAvailableLimit getAvailableLimit) {
        this.createCreditCard = createCreditCard;
        this.getAllCreditCards = getAllCreditCards;
        this.getCreditCardById = getCreditCardById;
        this.updateCreditCard = updateCreditCard;
        this.deleteCreditCard = deleteCreditCard;
        this.getInvoicesByCreditCard = getInvoicesByCreditCard;
        this.getAvailableLimit = getAvailableLimit;
    }

    @Operation(summary = "Criar novo cartão de crédito",
               description = "Cria um novo cartão de crédito no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cartão criado com sucesso",
                    content = @Content(mediaType = "application/json",
                                      schema = @Schema(implementation = CreditCardResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    })
    @PostMapping
    public ResponseEntity<CreditCardResponseDTO> createCreditCard(
            @Valid @RequestBody CreditCardRequestDTO requestDTO) {
        CreditCard creditCard = requestDTO.toCreditCard();
        CreditCard created = createCreditCard.execute(creditCard);
        CreditCardResponseDTO response = CreditCardResponseDTO.fromCreditCard(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Listar todos os cartões de crédito",
               description = "Retorna uma lista com todos os cartões de crédito cadastrados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
                    content = @Content(mediaType = "application/json",
                                      schema = @Schema(implementation = CreditCardResponseDTO.class)))
    })
    @GetMapping
    public ResponseEntity<List<CreditCardResponseDTO>> getAllCreditCards() {
        List<CreditCard> creditCards = getAllCreditCards.execute();
        List<CreditCardResponseDTO> response = creditCards.stream()
                .map(CreditCardResponseDTO::fromCreditCard)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar cartão de crédito por ID",
               description = "Retorna os detalhes de um cartão de crédito específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cartão encontrado com sucesso",
                    content = @Content(mediaType = "application/json",
                                      schema = @Schema(implementation = CreditCardResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cartão não encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<CreditCardResponseDTO> getCreditCardById(
            @Parameter(description = "ID do cartão a ser buscado", required = true)
            @PathVariable Long id) {
        Optional<CreditCard> creditCard = getCreditCardById.execute(id);

        return creditCard
                .map(cc -> ResponseEntity.ok(CreditCardResponseDTO.fromCreditCard(cc)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Atualizar cartão de crédito",
               description = "Atualiza os dados de um cartão de crédito existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cartão atualizado com sucesso",
                    content = @Content(mediaType = "application/json",
                                      schema = @Schema(implementation = CreditCardResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cartão não encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<CreditCardResponseDTO> updateCreditCard(
            @Parameter(description = "ID do cartão a ser atualizado", required = true)
            @PathVariable Long id,
            @Valid @RequestBody CreditCardRequestDTO requestDTO) {
        CreditCard creditCard = requestDTO.toCreditCard();
        Optional<CreditCard> updated = updateCreditCard.execute(id, creditCard);

        return updated
                .map(cc -> ResponseEntity.ok(CreditCardResponseDTO.fromCreditCard(cc)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Deletar cartão de crédito",
               description = "Remove um cartão de crédito do sistema. Atenção: esta ação não pode ser desfeita.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cartão deletado com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cartão não encontrado", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCreditCard(
            @Parameter(description = "ID do cartão a ser deletado", required = true)
            @PathVariable Long id) {
        boolean deleted = deleteCreditCard.execute(id);

        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Listar faturas do cartão de crédito",
               description = "Retorna todas as faturas de um cartão de crédito, ordenadas por mês de referência (mais recente primeiro).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Faturas retornadas com sucesso",
                    content = @Content(mediaType = "application/json",
                                      schema = @Schema(implementation = InvoiceResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cartão não encontrado", content = @Content)
    })
    @GetMapping("/{id}/invoices")
    public ResponseEntity<List<InvoiceResponseDTO>> getInvoicesByCreditCard(
            @Parameter(description = "ID do cartão de crédito", required = true)
            @PathVariable Long id) {
        // Verify credit card exists
        Optional<CreditCard> creditCard = getCreditCardById.execute(id);

        if (creditCard.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Invoice> invoices = getInvoicesByCreditCard.execute(id);
        List<InvoiceResponseDTO> response = invoices.stream()
                .map(InvoiceResponseDTO::fromInvoice)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obter limite disponível do cartão",
               description = "Calcula e retorna o limite disponível do cartão de crédito considerando parcelas em faturas abertas e pagamentos parciais.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Limite disponível calculado com sucesso",
                    content = @Content(mediaType = "application/json",
                                      schema = @Schema(implementation = AvailableLimitDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cartão não encontrado", content = @Content)
    })
    @GetMapping("/{id}/available-limit")
    public ResponseEntity<AvailableLimitDTO> getAvailableLimit(
            @Parameter(description = "ID do cartão de crédito", required = true)
            @PathVariable Long id) {
        // Verify credit card exists (will throw CreditCardNotFoundException if not found)
        // This is handled by GlobalExceptionHandler
        AvailableLimitResult result = getAvailableLimit.execute(id);
        AvailableLimitDTO response = AvailableLimitDTO.fromResult(result);
        return ResponseEntity.ok(response);
    }

}
