package com.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

public class DashboardDto {

    @Data @AllArgsConstructor
    public static class Summary {
        private BigDecimal totalIncome;
        private BigDecimal totalExpenses;
        private BigDecimal netBalance;
        private long transactionCount;
    }

    @Data @AllArgsConstructor
    public static class CategoryTotal {
        private String category;
        private BigDecimal total;
        private long count;
    }

    @Data @AllArgsConstructor
    public static class MonthlyTrend {
        private String month;
        private BigDecimal income;
        private BigDecimal expense;
        private BigDecimal net;
    }
}
