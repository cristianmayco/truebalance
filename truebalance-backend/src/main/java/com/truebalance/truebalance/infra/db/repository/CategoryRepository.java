package com.truebalance.truebalance.infra.db.repository;

import com.truebalance.truebalance.infra.db.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    Optional<CategoryEntity> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
