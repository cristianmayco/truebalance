package com.truebalance.truebalance.application.controller;

import com.truebalance.truebalance.application.dto.input.CategoryRequestDTO;
import com.truebalance.truebalance.application.dto.output.CategoryResponseDTO;
import com.truebalance.truebalance.application.dto.output.CategoryExpenseDTO;
import com.truebalance.truebalance.domain.entity.Category;
import com.truebalance.truebalance.domain.usecase.*;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/categories")
@Tag(name = "Categories", description = "API para gerenciamento de categorias de contas")
public class CategoryController {

    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    private final CreateCategory createCategory;
    private final GetAllCategories getAllCategories;
    private final GetCategoryById getCategoryById;
    private final UpdateCategory updateCategory;
    private final DeleteCategory deleteCategory;
    private final GetCategoryExpenses getCategoryExpenses;

    public CategoryController(CreateCategory createCategory,
                              GetAllCategories getAllCategories,
                              GetCategoryById getCategoryById,
                              UpdateCategory updateCategory,
                              DeleteCategory deleteCategory,
                              GetCategoryExpenses getCategoryExpenses) {
        this.createCategory = createCategory;
        this.getAllCategories = getAllCategories;
        this.getCategoryById = getCategoryById;
        this.updateCategory = updateCategory;
        this.deleteCategory = deleteCategory;
        this.getCategoryExpenses = getCategoryExpenses;
    }

    @Operation(summary = "Criar nova categoria",
               description = "Cria uma nova categoria de contas no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Categoria criada com sucesso",
                    content = @Content(mediaType = "application/json",
                                      schema = @Schema(implementation = CategoryResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou categoria já existe", content = @Content)
    })
    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(
            @Valid @RequestBody CategoryRequestDTO requestDTO) {
        logger.info("POST /categories - Criando categoria: nome={}", requestDTO.getName());
        
        Category category = new Category();
        category.setName(requestDTO.getName());
        category.setDescription(requestDTO.getDescription());
        category.setColor(requestDTO.getColor());
        
        Category created = createCategory.execute(category);
        CategoryResponseDTO response = CategoryResponseDTO.fromCategory(created);
        
        logger.info("Categoria criada com sucesso! ID={}, nome={}", response.getId(), response.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Listar todas as categorias",
               description = "Retorna uma lista com todas as categorias cadastradas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories() {
        logger.info("GET /categories - Listando todas as categorias");
        List<Category> categories = getAllCategories.execute();
        List<CategoryResponseDTO> response = categories.stream()
                .map(CategoryResponseDTO::fromCategory)
                .collect(Collectors.toList());
        logger.info("Retornando {} categorias", response.size());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar categoria por ID",
               description = "Retorna os detalhes de uma categoria específica.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoria encontrada com sucesso",
                    content = @Content(mediaType = "application/json",
                                      schema = @Schema(implementation = CategoryResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(
            @Parameter(description = "ID da categoria", required = true)
            @PathVariable Long id) {
        logger.info("GET /categories/{} - Buscando categoria", id);
        Category category = getCategoryById.execute(id);
        CategoryResponseDTO response = CategoryResponseDTO.fromCategory(category);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Atualizar categoria",
               description = "Atualiza os dados de uma categoria existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoria atualizada com sucesso",
                    content = @Content(mediaType = "application/json",
                                      schema = @Schema(implementation = CategoryResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada", content = @Content),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou nome já existe", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(
            @Parameter(description = "ID da categoria", required = true)
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequestDTO requestDTO) {
        logger.info("PUT /categories/{} - Atualizando categoria: nome={}", id, requestDTO.getName());
        
        Category category = new Category();
        category.setName(requestDTO.getName());
        category.setDescription(requestDTO.getDescription());
        category.setColor(requestDTO.getColor());
        
        Category updated = updateCategory.execute(id, category);
        CategoryResponseDTO response = CategoryResponseDTO.fromCategory(updated);
        
        logger.info("Categoria atualizada com sucesso! ID={}, nome={}", response.getId(), response.getName());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Deletar categoria",
               description = "Remove uma categoria do sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Categoria deletada com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "ID da categoria", required = true)
            @PathVariable Long id) {
        logger.info("DELETE /categories/{} - Deletando categoria", id);
        deleteCategory.execute(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Buscar gastos da categoria",
               description = "Retorna os gastos da categoria agrupados por período (mês ou ano).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gastos retornados com sucesso",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada", content = @Content)
    })
    @GetMapping("/{id}/expenses")
    public ResponseEntity<List<CategoryExpenseDTO>> getCategoryExpenses(
            @Parameter(description = "ID da categoria", required = true)
            @PathVariable Long id,
            @Parameter(description = "Tipo de agrupamento: 'monthly' para mensal ou 'yearly' para anual", required = true)
            @RequestParam(defaultValue = "monthly") String period) {
        logger.info("GET /categories/{}/expenses - Buscando gastos da categoria, período={}", id, period);
        
        Category category = getCategoryById.execute(id);
        List<CategoryExpenseDTO> expenses;
        
        if ("yearly".equalsIgnoreCase(period)) {
            expenses = getCategoryExpenses.executeYearly(id, category.getName());
        } else {
            expenses = getCategoryExpenses.executeMonthly(id, category.getName());
        }
        
        logger.info("Retornando {} períodos de gastos", expenses.size());
        return ResponseEntity.ok(expenses);
    }
}
