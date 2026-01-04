package com.truebalance.truebalance.domain.port;

import com.truebalance.truebalance.domain.entity.Bill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BillRepositoryPort {

    Bill save(Bill bill);

    Optional<Bill> findById(Long id);

    List<Bill> findAll();

    Page<Bill> findAll(Pageable pageable);

    Page<Bill> findAll(Pageable pageable, String name, LocalDateTime startDate, LocalDateTime endDate);

    Page<Bill> findAll(Pageable pageable, String name, LocalDateTime startDate, LocalDateTime endDate,
                       java.math.BigDecimal minAmount, java.math.BigDecimal maxAmount,
                       Integer numberOfInstallments, String category, Long creditCardId, Boolean hasCreditCard);

    /**
     * Busca todas as contas de uma categoria específica dentro de um intervalo de datas
     */
    List<Bill> findAllByCategoryAndDateRange(String categoryName, LocalDateTime startDate, LocalDateTime endDate);

    void deleteById(Long id);
}
