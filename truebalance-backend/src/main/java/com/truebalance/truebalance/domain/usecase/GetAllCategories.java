package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.domain.entity.Category;
import com.truebalance.truebalance.domain.port.CategoryRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class GetAllCategories {

    private static final Logger logger = LoggerFactory.getLogger(GetAllCategories.class);
    private final CategoryRepositoryPort repository;

    public GetAllCategories(CategoryRepositoryPort repository) {
        this.repository = repository;
    }

    public List<Category> execute() {
        logger.info("Buscando todas as categorias");
        List<Category> categories = repository.findAll();
        logger.info("Total de categorias encontradas: {}", categories.size());
        return categories;
    }
}
