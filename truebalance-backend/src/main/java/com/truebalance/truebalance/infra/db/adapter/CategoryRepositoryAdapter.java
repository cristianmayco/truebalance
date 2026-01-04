package com.truebalance.truebalance.infra.db.adapter;

import com.truebalance.truebalance.domain.entity.Category;
import com.truebalance.truebalance.domain.port.CategoryRepositoryPort;
import com.truebalance.truebalance.infra.db.entity.CategoryEntity;
import com.truebalance.truebalance.infra.db.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CategoryRepositoryAdapter implements CategoryRepositoryPort {

    private static final Logger logger = LoggerFactory.getLogger(CategoryRepositoryAdapter.class);
    private final CategoryRepository repository;

    public CategoryRepositoryAdapter(CategoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public Category save(Category category) {
        logger.debug("Salvando categoria: nome={}", category.getName());
        CategoryEntity entity = toEntity(category);
        CategoryEntity savedEntity = repository.save(entity);
        logger.debug("Categoria salva com sucesso! ID={}", savedEntity.getId());
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Category> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Category> findByNameIgnoreCase(String name) {
        return repository.findByNameIgnoreCase(name).map(this::toDomain);
    }

    @Override
    public List<Category> findAll() {
        logger.debug("Buscando todas as categorias");
        return repository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        logger.debug("Deletando categoria ID={}", id);
        repository.deleteById(id);
    }

    @Override
    public boolean existsByNameIgnoreCase(String name) {
        return repository.existsByNameIgnoreCase(name);
    }

    private CategoryEntity toEntity(Category category) {
        CategoryEntity entity = new CategoryEntity();
        entity.setId(category.getId());
        entity.setName(category.getName());
        entity.setDescription(category.getDescription());
        entity.setColor(category.getColor());
        // createdAt e updatedAt são gerenciados automaticamente pelo JPA
        return entity;
    }

    private Category toDomain(CategoryEntity entity) {
        Category category = new Category();
        category.setId(entity.getId());
        category.setName(entity.getName());
        category.setDescription(entity.getDescription());
        category.setColor(entity.getColor());
        category.setCreatedAt(entity.getCreatedAt());
        category.setUpdatedAt(entity.getUpdatedAt());
        return category;
    }
}
