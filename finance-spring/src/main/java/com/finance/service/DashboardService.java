package com.finance.service;

import com.finance.dto.DashboardDto;
import com.finance.entity.Transaction;
import com.finance.enums.TransactionType;
import com.finance.repository.TransactionRepository;
import com.finance.repository.TransactionSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TransactionRepository transactionRepository;

    public DashboardDto.Summary getSummary(LocalDate dateFrom, LocalDate dateTo) {
        var spec = TransactionSpec.filter(null, null, dateFrom, dateTo, null);
        var all  = transactionRepository.findAll(spec);

        BigDecimal income  = sum(all, TransactionType.INCOME);
        BigDecimal expense = sum(all, TransactionType.EXPENSE);

        return new DashboardDto.Summary(
            income,
            expense,
            income.subtract(expense),
            all.size()
        );
    }

    public List<DashboardDto.CategoryTotal> getCategoryTotals(TransactionType type) {
        // Pass enum name as String for native query; null means "all types"
        String typeStr = (type != null) ? type.name() : null;
        return transactionRepository.getCategoryTotals(typeStr).stream()
                .map(row -> new DashboardDto.CategoryTotal(
                    (String) row[0],
                    toBigDecimal(row[1]),
                    ((Number) row[2]).longValue()
                ))
                .toList();
    }

    public List<DashboardDto.MonthlyTrend> getMonthlyTrends(int months) {
        LocalDate from = LocalDate.now().minusMonths(months);
        return transactionRepository.getMonthlyTrends(from).stream()
                .map(row -> {
                    BigDecimal inc = toBigDecimal(row[1]);
                    BigDecimal exp = toBigDecimal(row[2]);
                    return new DashboardDto.MonthlyTrend(
                        (String) row[0],
                        inc,
                        exp,
                        inc.subtract(exp)
                    );
                })
                .toList();
    }

    public List<Transaction> getRecentActivity(int limit) {
        var spec     = TransactionSpec.filter(null, null, null, null, null);
        var pageable = PageRequest.of(
            0, limit,
            Sort.by("date").descending().and(Sort.by("id").descending())
        );
        return transactionRepository.findAll(spec, pageable).getContent();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private BigDecimal sum(List<Transaction> txs, TransactionType type) {
        return txs.stream()
                .filter(t -> t.getType() == type)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal bd) return bd;
        return new BigDecimal(value.toString());
    }
}
