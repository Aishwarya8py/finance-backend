package com.finance.controller;

import com.finance.dto.DashboardDto;
import com.finance.dto.TransactionDto;
import com.finance.enums.TransactionType;
import com.finance.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    @Autowired
    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardDto.Summary> summary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {

        return ResponseEntity.ok(dashboardService.getSummary(dateFrom, dateTo));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<DashboardDto.CategoryTotal>> categories(
            @RequestParam(required = false) TransactionType type) {
        return ResponseEntity.ok(dashboardService.getCategoryTotals(type));
    }

    @GetMapping("/trends")
    public ResponseEntity<List<DashboardDto.MonthlyTrend>> trends(
            @RequestParam(defaultValue = "12") int months) {

        if (months < 1 || months > 60) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(dashboardService.getMonthlyTrends(months));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<TransactionDto.Response>> recent(
            @RequestParam(defaultValue = "10") int limit) {

        if (limit < 1 || limit > 50) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(
            dashboardService.getRecentActivity(limit)
                .stream().map(TransactionDto.Response::from).toList()
        );
    }
}
