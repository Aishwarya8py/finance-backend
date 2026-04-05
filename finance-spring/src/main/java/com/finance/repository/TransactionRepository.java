package com.finance.repository;

import com.finance.entity.Transaction;
import com.finance.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    // ── Dashboard aggregations (native SQL for full H2 compatibility) ─────────

    @Query(value = """
        SELECT category,
               SUM(amount) AS total,
               COUNT(*)    AS cnt
        FROM transactions
        WHERE is_deleted = false
          AND (:type IS NULL OR type = :type)
        GROUP BY category
        ORDER BY total DESC
        """, nativeQuery = true)
    List<Object[]> getCategoryTotals(@Param("type") String type);

    @Query(value = """
        SELECT FORMATDATETIME(date, 'yyyy-MM')                         AS month,
               SUM(CASE WHEN type = 'INCOME'  THEN amount ELSE 0 END) AS income,
               SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) AS expense
        FROM transactions
        WHERE is_deleted = false
          AND date >= :fromDate
        GROUP BY month
        ORDER BY month ASC
        """, nativeQuery = true)
    List<Object[]> getMonthlyTrends(@Param("fromDate") LocalDate fromDate);
}
