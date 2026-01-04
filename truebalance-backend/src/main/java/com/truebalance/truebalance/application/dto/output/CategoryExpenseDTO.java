package com.truebalance.truebalance.application.dto.output;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CategoryExpenseDTO {
    private LocalDate period;  // Data do período (mês ou ano)
    private BigDecimal totalAmount;  // Valor total gasto no período
    private Long billCount;  // Quantidade de contas no período

    public CategoryExpenseDTO() {
    }

    public CategoryExpenseDTO(LocalDate period, BigDecimal totalAmount, Long billCount) {
        this.period = period;
        this.totalAmount = totalAmount;
        this.billCount = billCount;
    }

    public LocalDate getPeriod() {
        return period;
    }

    public void setPeriod(LocalDate period) {
        this.period = period;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Long getBillCount() {
        return billCount;
    }

    public void setBillCount(Long billCount) {
        this.billCount = billCount;
    }
}
