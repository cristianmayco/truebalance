package com.truebalance.truebalance.application.controller;

import com.truebalance.truebalance.application.dto.input.BillRequestDTO;
import com.truebalance.truebalance.application.dto.output.BillResponseDTO;
import com.truebalance.truebalance.application.dto.output.InstallmentResponseDTO;
import com.truebalance.truebalance.application.dto.output.PaginatedResponse;
import com.truebalance.truebalance.domain.entity.Bill;
import com.truebalance.truebalance.domain.entity.Installment;
import com.truebalance.truebalance.domain.usecase.CreateBill;
import com.truebalance.truebalance.domain.usecase.CreateBillWithCreditCard;
import com.truebalance.truebalance.domain.usecase.DeleteBill;
import com.truebalance.truebalance.domain.usecase.GetAllBills;
import com.truebalance.truebalance.domain.usecase.GetBillById;
import com.truebalance.truebalance.domain.usecase.GetBillInstallments;
import com.truebalance.truebalance.domain.usecase.UpdateBill;
import com.truebalance.truebalance.domain.usecase.UpdateBillWithCreditCard;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/bills")
@Tag(name = "Bills", description = "API para gerenciamento de contas e despesas")
public class BillController {

    private static final Logger logger = LoggerFactory.getLogger(BillController.class);
    private final CreateBill createBill;
    private final CreateBillWithCreditCard createBillWithCreditCard;
    private final UpdateBill updateBill;
    private final UpdateBillWithCreditCard updateBillWithCreditCard;
    private final GetAllBills getAllBills;
    private final GetBillById getBillById;
    private final DeleteBill deleteBill;
    private final GetBillInstallments getBillInstallments;

    public BillController(CreateBill createBill, CreateBillWithCreditCard createBillWithCreditCard,
                          UpdateBill updateBill, UpdateBillWithCreditCard updateBillWithCreditCard,
                          GetAllBills getAllBills, GetBillById getBillById, DeleteBill deleteBill,
                          GetBillInstallments getBillInstallments) {
        this.createBill = createBill;
        this.createBillWithCreditCard = createBillWithCreditCard;
        this.updateBill = updateBill;
        this.updateBillWithCreditCard = updateBillWithCreditCard;
        this.getAllBills = getAllBills;
        this.getBillById = getBillById;
        this.deleteBill = deleteBill;
        this.getBillInstallments = getBillInstallments;
    }

    @Operation(summary = "Listar todas as contas", description = "Retorna uma lista paginada com todas as contas/despesas cadastradas no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de contas retornada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaginatedResponse.class)))
    })
    @GetMapping
    public ResponseEntity<PaginatedResponse<BillResponseDTO>> getAllBills(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        logger.info("GET /bills - page={}, size={}, sort={}, name={}, startDate={}, endDate={}", 
            page, size, sort, name, startDate, endDate);

        // Parse sort parameter (format: "field,direction" e.g., "executionDate,desc")
        Sort sortObj = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            String[] sortParts = sort.split(",");
            if (sortParts.length == 2) {
                String field = sortParts[0].trim();
                String direction = sortParts[1].trim();
                sortObj = direction.equalsIgnoreCase("desc") 
                    ? Sort.by(Sort.Direction.DESC, field)
                    : Sort.by(Sort.Direction.ASC, field);
            }
        } else {
            // Default sort by executionDate desc
            sortObj = Sort.by(Sort.Direction.DESC, "executionDate");
        }

        Pageable pageable = PageRequest.of(page, size, sortObj);

        // Parse dates
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        
        try {
            if (startDate != null && !startDate.isEmpty()) {
                startDateTime = LocalDateTime.parse(startDate, formatter);
            }
            if (endDate != null && !endDate.isEmpty()) {
                endDateTime = LocalDateTime.parse(endDate, formatter);
            }
        } catch (Exception e) {
            logger.warn("Erro ao fazer parse das datas: {}", e.getMessage());
        }

        Page<Bill> billsPage;
        if (name != null || startDateTime != null || endDateTime != null) {
            billsPage = getAllBills.execute(pageable, name, startDateTime, endDateTime);
        } else {
            billsPage = getAllBills.execute(pageable);
        }

        // Map bills to DTOs and include creditCardId from installments
        // Note: For performance, we only fetch creditCardId for individual bill requests
        // In list view, creditCardId will be null to avoid N+1 query problem
        List<BillResponseDTO> content = billsPage.getContent().stream()
                .map(bill -> BillResponseDTO.fromBill(bill, null))  // creditCardId not included in list for performance
                .collect(Collectors.toList());

        PaginatedResponse<BillResponseDTO> response = new PaginatedResponse<>(
            content,
            billsPage.getNumber(),
            billsPage.getSize(),
            billsPage.getTotalElements(),
            billsPage.getTotalPages()
        );

        logger.info("Retornando {} contas (página {} de {})", 
            content.size(), billsPage.getNumber() + 1, billsPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Criar uma nova conta",
               description = "Cria uma nova conta/despesa no sistema. O valor das parcelas é calculado automaticamente. " +
                             "Se creditCardId for fornecido, a conta será vinculada ao cartão de crédito e as parcelas " +
                             "serão automaticamente distribuídas nas faturas correspondentes baseadas no ciclo de faturamento.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Conta criada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BillResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou limite de crédito insuficiente", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cartão de crédito não encontrado", content = @Content)
    })
    @PostMapping
    public ResponseEntity<BillResponseDTO> addBill(@Valid @RequestBody BillRequestDTO billRequestDTO) {
        logger.info("POST /bills - Criando nova conta: nome={}, valorTotal={}, parcelas={}, dataExecucao={}, creditCardId={}", 
            billRequestDTO.getName(), billRequestDTO.getTotalAmount(), 
            billRequestDTO.getNumberOfInstallments(), billRequestDTO.getExecutionDate(),
            billRequestDTO.getCreditCardId());
        
        Bill bill = billRequestDTO.toBill();
        Bill createdBill;

        // Decisão: com ou sem cartão?
        if (billRequestDTO.getCreditCardId() != null) {
            logger.info("Criando conta vinculada ao cartão de crédito ID={}", billRequestDTO.getCreditCardId());
            // Fluxo integrado com cartão de crédito
            createdBill = createBillWithCreditCard.execute(bill, billRequestDTO.getCreditCardId());
        } else {
            logger.info("Criando conta standalone (sem cartão de crédito)");
            // Fluxo standalone (comportamento atual - sem integração com cartão)
            createdBill = createBill.addBill(bill);
        }

        BillResponseDTO response = BillResponseDTO.fromBill(createdBill);
        logger.info("Conta criada com sucesso! ID={}, nome={}", response.getId(), response.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Atualizar uma conta existente", description = "Atualiza os dados de uma conta/despesa existente. O valor das parcelas é recalculado automaticamente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta atualizada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BillResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada", content = @Content),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<BillResponseDTO> updateBill(
            @Parameter(description = "ID da conta a ser atualizada", required = true)
            @PathVariable Long id,
            @Valid @RequestBody BillRequestDTO billRequestDTO) {
        logger.info("PUT /bills/{} - Atualizando conta: nome={}, valorTotal={}, parcelas={}, dataExecucao={}, creditCardId={}", 
            id, billRequestDTO.getName(), billRequestDTO.getTotalAmount(), 
            billRequestDTO.getNumberOfInstallments(), billRequestDTO.getExecutionDate(),
            billRequestDTO.getCreditCardId());
        
        Bill bill = billRequestDTO.toBill();
        Bill updatedBill;

        // Check if bill was previously linked to a credit card
        List<Installment> existingInstallments = getBillInstallments.execute(id);
        boolean wasLinkedToCard = existingInstallments != null && !existingInstallments.isEmpty();
        
        // Decisão: com ou sem cartão?
        if (billRequestDTO.getCreditCardId() != null) {
            logger.info("Atualizando conta vinculada ao cartão de crédito ID={}", billRequestDTO.getCreditCardId());
            // Fluxo integrado com cartão de crédito
            updatedBill = updateBillWithCreditCard.execute(id, bill, billRequestDTO.getCreditCardId());
        } else {
            logger.info("Atualizando conta standalone (sem cartão de crédito)");
            // Se a conta estava vinculada a um cartão, precisamos remover os installments
            if (wasLinkedToCard) {
                logger.info("Removendo installments da conta que estava vinculada a cartão");
                // Remove installments and update invoices
                updateBillWithCreditCard.removeInstallmentsAndUpdateInvoices(id, existingInstallments);
            }
            
            // Fluxo standalone (comportamento atual - sem integração com cartão)
            Optional<Bill> result = updateBill.updateBill(id, bill);
            if (result.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            updatedBill = result.get();
        }

        // Get creditCardId from installments if bill is linked to a credit card
        Long creditCardId = billRequestDTO.getCreditCardId();  // Default to request value
        try {
            List<Installment> installments = getBillInstallments.execute(id);
            if (installments != null && !installments.isEmpty()) {
                creditCardId = installments.stream()
                        .map(Installment::getCreditCardId)
                        .filter(ccId -> ccId != null)
                        .findFirst()
                        .orElse(billRequestDTO.getCreditCardId());
            }
        } catch (Exception e) {
            logger.warn("Error fetching installments for bill {}: {}", id, e.getMessage());
            // Use creditCardId from request if we can't fetch installments
        }

        BillResponseDTO response = BillResponseDTO.fromBill(updatedBill, creditCardId);
        logger.info("Conta atualizada com sucesso! ID={}, nome={}", response.getId(), response.getName());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar conta por ID", description = "Retorna os detalhes de uma conta específica.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta encontrada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BillResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<BillResponseDTO> getBillById(
            @Parameter(description = "ID da conta a ser buscada", required = true)
            @PathVariable Long id) {
        Optional<Bill> bill = getBillById.execute(id);

        if (bill.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Get creditCardId from installments if bill is linked to a credit card
        Long creditCardId = null;
        try {
            List<Installment> installments = getBillInstallments.execute(id);
            if (installments != null && !installments.isEmpty()) {
                creditCardId = installments.stream()
                        .map(Installment::getCreditCardId)
                        .filter(ccId -> ccId != null)
                        .findFirst()
                        .orElse(null);
            }
        } catch (Exception e) {
            logger.warn("Error fetching installments for bill {}: {}", id, e.getMessage());
            // creditCardId remains null if we can't fetch installments
        }

        BillResponseDTO response = BillResponseDTO.fromBill(bill.get(), creditCardId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Deletar conta", description = "Remove uma conta do sistema. Atenção: esta ação não pode ser desfeita.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Conta deletada com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBill(
            @Parameter(description = "ID da conta a ser deletada", required = true)
            @PathVariable Long id) {
        boolean deleted = deleteBill.execute(id);

        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Listar parcelas da conta",
               description = "Retorna todas as parcelas de uma conta específica, ordenadas por número de parcela.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parcelas retornadas com sucesso",
                    content = @Content(mediaType = "application/json",
                                      schema = @Schema(implementation = InstallmentResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada", content = @Content)
    })
    @GetMapping("/{id}/installments")
    public ResponseEntity<List<InstallmentResponseDTO>> getBillInstallments(
            @Parameter(description = "ID da conta", required = true)
            @PathVariable Long id) {
        // Verify bill exists
        Optional<Bill> bill = getBillById.execute(id);

        if (bill.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Installment> installments = getBillInstallments.execute(id);
        List<InstallmentResponseDTO> response = installments.stream()
                .map(InstallmentResponseDTO::fromInstallment)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

}
