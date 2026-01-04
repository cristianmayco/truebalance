package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.domain.entity.Category;
import com.truebalance.truebalance.domain.exception.CategoryNotFoundException;
import com.truebalance.truebalance.domain.exception.CategoryAlreadyExistsException;
import com.truebalance.truebalance.domain.port.CategoryRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class UpdateCategory {

    private static final Logger logger = LoggerFactory.getLogger(UpdateCategory.class);
    private final CategoryRepositoryPort repository;

    public UpdateCategory(CategoryRepositoryPort repository) {
        this.repository = repository;
    }

    public Category execute(Long id, Category category) {
        logger.info("Atualizando categoria ID={}, nome={}", id, category.getName());

        // Verificar se categoria existe
        Optional<Category> existingCategory = repository.findById(id);
        if (existingCategory.isEmpty()) {
            logger.warn("Categoria com ID {} não encontrada", id);
            throw new CategoryNotFoundException(id);
        }

        // Verificar se novo nome já existe (caso tenha mudado)
        if (!existingCategory.get().getName().equalsIgnoreCase(category.getName())) {
            if (repository.existsByNameIgnoreCase(category.getName())) {
                logger.warn("Categoria com nome '{}' já existe", category.getName());
                throw new CategoryAlreadyExistsException(category.getName());
            }
        }

        category.setId(id);
        Category updatedCategory = repository.save(category);
        logger.info("Categoria atualizada com sucesso! ID={}, nome={}", updatedCategory.getId(), updatedCategory.getName());
        return updatedCategory;
    }
}
