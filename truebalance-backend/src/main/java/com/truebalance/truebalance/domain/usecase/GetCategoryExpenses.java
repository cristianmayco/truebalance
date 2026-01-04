package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.application.dto.output.CategoryExpenseDTO;
import com.truebalance.truebalance.domain.entity.Bill;
import com.truebalance.truebalance.domain.port.BillRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GetCategoryExpenses {

    private static final Logger logger = LoggerFactory.getLogger(GetCategoryExpenses.class);
    private final BillRepositoryPort repository;

    public GetCategoryExpenses(BillRepositoryPort repository) {
        this.repository = repository;
    }

    /**
     * Busca gastos por categoria, agrupados por mês (últimos 12 meses)
     */
    public List<CategoryExpenseDTO> executeMonthly(Long categoryId, String categoryName) {
        logger.info("Buscando gastos mensais para categoria ID={}, nome={}", categoryId, categoryName);
        
        // Calcular data de início (12 meses atrás)
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(11).withDayOfMonth(1); // Primeiro dia do mês há 12 meses
        
        // Buscar todas as contas da categoria no período
        List<Bill> bills = repository.findAllByCategoryAndDateRange(categoryName, 
                startDate.atStartOfDay(), 
                endDate.atTime(23, 59, 59));
        
        // Agrupar por mês
        Map<YearMonth, List<Bill>> billsByMonth = bills.stream()
                .collect(Collectors.groupingBy(bill -> 
                    YearMonth.from(bill.getExecutionDate())));
        
        // Criar lista de resultados para os últimos 12 meses (mais recente primeiro)
        List<CategoryExpenseDTO> expenses = new ArrayList<>();
        for (int i = 0; i <= 11; i++) {
            YearMonth month = YearMonth.from(endDate.minusMonths(i));
            List<Bill> monthBills = billsByMonth.getOrDefault(month, new ArrayList<>());
            
            BigDecimal total = monthBills.stream()
                    .map(Bill::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            expenses.add(new CategoryExpenseDTO(
                    month.atDay(1), // Primeiro dia do mês para representar o período
                    total,
                    (long) monthBills.size()
            ));
        }
        
        logger.info("Encontrados {} períodos mensais com gastos", expenses.size());
        return expenses;
    }

    /**
     * Busca gastos por categoria, acumulados por ano (últimos 12 anos)
     */
    public List<CategoryExpenseDTO> executeYearly(Long categoryId, String categoryName) {
        logger.info("Buscando gastos anuais acumulados para categoria ID={}, nome={}", categoryId, categoryName);
        
        // Calcular data de início (12 anos atrás)
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusYears(11).withDayOfYear(1); // Primeiro dia do ano há 12 anos
        
        // Buscar todas as contas da categoria no período
        List<Bill> bills = repository.findAllByCategoryAndDateRange(categoryName, 
                startDate.atStartOfDay(), 
                endDate.atTime(23, 59, 59));
        
        // Agrupar por ano
        Map<Integer, List<Bill>> billsByYear = bills.stream()
                .collect(Collectors.groupingBy(bill -> 
                    bill.getExecutionDate().getYear()));
        
        // Criar lista de resultados para os últimos 12 anos (mais recente primeiro)
        List<CategoryExpenseDTO> expenses = new ArrayList<>();
        int currentYear = endDate.getYear();
        for (int i = 0; i <= 11; i++) {
            int year = currentYear - i;
            List<Bill> yearBills = billsByYear.getOrDefault(year, new ArrayList<>());
            
            BigDecimal total = yearBills.stream()
                    .map(Bill::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            expenses.add(new CategoryExpenseDTO(
                    LocalDate.of(year, 1, 1), // Primeiro dia do ano para representar o período
                    total,
                    (long) yearBills.size()
            ));
        }
        
        logger.info("Encontrados {} períodos anuais com gastos", expenses.size());
        return expenses;
    }
}
