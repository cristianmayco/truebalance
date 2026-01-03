package com.truebalance.truebalance.infra.db.repository;

import com.truebalance.truebalance.infra.db.entity.BillEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface BillRepository extends JpaRepository<BillEntity, Long> {

    @Query("SELECT b FROM BillEntity b WHERE " +
           "(:name IS NULL OR LOWER(b.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:startDate IS NULL OR b.executionDate >= :startDate) AND " +
           "(:endDate IS NULL OR b.executionDate <= :endDate)")
    Page<BillEntity> findAll(Pageable pageable, 
                             @Param("name") String name,
                             @Param("startDate") LocalDateTime startDate,
                             @Param("endDate") LocalDateTime endDate);
}
