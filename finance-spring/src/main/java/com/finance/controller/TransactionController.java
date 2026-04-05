package com.finance.controller;

import com.finance.dto.PagedResponse;
import com.finance.dto.TransactionDto;
import com.finance.entity.User;
import com.finance.enums.TransactionType;
import com.finance.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    // All authenticated users can read
    @GetMapping
    public ResponseEntity<PagedResponse<TransactionDto.Response>> list(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "20") int limit) {

        var result = transactionService.list(type, category, dateFrom, dateTo, search, page, limit);
        return ResponseEntity.ok(PagedResponse.of(result, TransactionDto.Response::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(TransactionDto.Response.from(transactionService.findById(id)));
    }

    // Analyst and Admin can create
    @PostMapping
    @PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
    public ResponseEntity<TransactionDto.Response> create(
            @Valid @RequestBody TransactionDto.CreateRequest req,
            @AuthenticationPrincipal User currentUser) {

        var tx = transactionService.create(req, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(TransactionDto.Response.from(tx));
    }

    // Analyst and Admin can update
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
    public ResponseEntity<TransactionDto.Response> update(
            @PathVariable Long id,
            @Valid @RequestBody TransactionDto.UpdateRequest req) {

        return ResponseEntity.ok(TransactionDto.Response.from(transactionService.update(id, req)));
    }

    // Only Admin can delete
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        transactionService.softDelete(id);
        return ResponseEntity.ok(Map.of("message", "Transaction deleted successfully."));
    }
}
