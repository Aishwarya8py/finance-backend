package com.finance.dto;

import com.finance.entity.Transaction;
import com.finance.enums.TransactionType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TransactionDto {

    @Data
    public static class CreateRequest {
        @NotNull @DecimalMin(value = "0.01", message = "Amount must be positive")
        private BigDecimal amount;

        @NotNull
        private TransactionType type;

        @NotBlank @Size(max = 100)
        private String category;

        @NotNull
        private LocalDate date;

        @Size(max = 500)
        private String notes;
    }

    @Data
    public static class UpdateRequest {
        @DecimalMin(value = "0.01", message = "Amount must be positive")
        private BigDecimal amount;

        private TransactionType type;

        @Size(max = 100)
        private String category;

        private LocalDate date;

        @Size(max = 500)
        private String notes;
    }

    @Data
    public static class Response {
        private Long id;
        private Long userId;
        private BigDecimal amount;
        private TransactionType type;
        private String category;
        private LocalDate date;
        private String notes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response from(Transaction t) {
            Response r = new Response();
            r.id        = t.getId();
            r.userId    = t.getUser().getId();
            r.amount    = t.getAmount();
            r.type      = t.getType();
            r.category  = t.getCategory();
            r.date      = t.getDate();
            r.notes     = t.getNotes();
            r.createdAt = t.getCreatedAt();
            r.updatedAt = t.getUpdatedAt();
            return r;
        }
    }
}
