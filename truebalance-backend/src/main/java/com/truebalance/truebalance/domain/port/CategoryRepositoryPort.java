package com.truebalance.truebalance.domain.port;

import com.truebalance.truebalance.domain.entity.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepositoryPort {
    Category save(Category category);
    Optional<Category> findById(Long id);
    Optional<Category> findByNameIgnoreCase(String name);
    List<Category> findAll();
    void deleteById(Long id);
    boolean existsByNameIgnoreCase(String name);
}
