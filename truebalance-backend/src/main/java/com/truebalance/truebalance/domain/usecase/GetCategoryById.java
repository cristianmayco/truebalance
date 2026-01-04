package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.domain.entity.Category;
import com.truebalance.truebalance.domain.exception.CategoryNotFoundException;
import com.truebalance.truebalance.domain.port.CategoryRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class GetCategoryById {

    private static final Logger logger = LoggerFactory.getLogger(GetCategoryById.class);
    private final CategoryRepositoryPort repository;

    public GetCategoryById(CategoryRepositoryPort repository) {
        this.repository = repository;
    }

    public Category execute(Long id) {
        logger.info("Buscando categoria por ID: {}", id);
        Optional<Category> category = repository.findById(id);
        
        if (category.isEmpty()) {
            logger.warn("Categoria com ID {} não encontrada", id);
            throw new CategoryNotFoundException(id);
        }
        
        logger.info("Categoria encontrada: ID={}, nome={}", category.get().getId(), category.get().getName());
        return category.get();
    }
}
