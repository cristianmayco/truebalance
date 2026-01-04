package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.domain.entity.Category;
import com.truebalance.truebalance.domain.exception.CategoryAlreadyExistsException;
import com.truebalance.truebalance.domain.port.CategoryRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateCategory {

    private static final Logger logger = LoggerFactory.getLogger(CreateCategory.class);
    private final CategoryRepositoryPort repository;

    public CreateCategory(CategoryRepositoryPort repository) {
        this.repository = repository;
    }

    public Category execute(Category category) {
        logger.info("Criando categoria: nome={}", category.getName());

        // Verificar se já existe categoria com mesmo nome (case-insensitive)
        if (repository.existsByNameIgnoreCase(category.getName())) {
            logger.warn("Categoria com nome '{}' já existe", category.getName());
            throw new CategoryAlreadyExistsException(category.getName());
        }

        Category savedCategory = repository.save(category);
        logger.info("Categoria criada com sucesso! ID={}, nome={}", savedCategory.getId(), savedCategory.getName());
        return savedCategory;
    }
}
