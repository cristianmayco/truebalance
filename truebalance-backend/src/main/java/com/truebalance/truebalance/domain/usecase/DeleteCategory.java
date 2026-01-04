package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.domain.exception.CategoryNotFoundException;
import com.truebalance.truebalance.domain.port.CategoryRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class DeleteCategory {

    private static final Logger logger = LoggerFactory.getLogger(DeleteCategory.class);
    private final CategoryRepositoryPort repository;

    public DeleteCategory(CategoryRepositoryPort repository) {
        this.repository = repository;
    }

    public boolean execute(Long id) {
        logger.info("Deletando categoria ID={}", id);

        // Verificar se categoria existe
        Optional<com.truebalance.truebalance.domain.entity.Category> category = repository.findById(id);
        if (category.isEmpty()) {
            logger.warn("Categoria com ID {} não encontrada", id);
            throw new CategoryNotFoundException(id);
        }

        repository.deleteById(id);
        logger.info("Categoria deletada com sucesso! ID={}, nome={}", id, category.get().getName());
        return true;
    }
}
