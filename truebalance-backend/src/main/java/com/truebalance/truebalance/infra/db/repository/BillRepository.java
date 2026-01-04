package com.truebalance.truebalance.infra.db.repository;

import com.truebalance.truebalance.infra.db.entity.BillEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<BillEntity, Long>, JpaSpecificationExecutor<BillEntity> {

    @Query("SELECT b FROM BillEntity b WHERE " +
           "COALESCE(:name, '') = '' OR LOWER(b.name) LIKE CONCAT('%', LOWER(COALESCE(:name, '')), '%')")
    Page<BillEntity> findAll(Pageable pageable,
                             @Param("name") String name,
                             @Param("startDate") LocalDateTime startDate,
                             @Param("endDate") LocalDateTime endDate);


    @Query("SELECT b FROM BillEntity b WHERE " +
           "LOWER(b.name) = LOWER(:name) AND " +
           "b.totalAmount = :totalAmount AND " +
           "b.executionDate = :executionDate AND " +
           "b.numberOfInstallments = :numberOfInstallments")
    Optional<BillEntity> findDuplicate(
            @Param("name") String name,
            @Param("totalAmount") BigDecimal totalAmount,
            @Param("executionDate") LocalDateTime executionDate,
            @Param("numberOfInstallments") int numberOfInstallments
    );
}
