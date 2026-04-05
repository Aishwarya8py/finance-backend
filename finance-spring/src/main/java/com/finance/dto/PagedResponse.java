package com.finance.dto;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

@Data
public class PagedResponse<T> {
    private List<T> data;
    private PaginationMeta pagination;

    @Data
    public static class PaginationMeta {
        private int page;
        private int limit;
        private long total;
        private int pages;
    }

    public static <E, T> PagedResponse<T> of(Page<E> page, Function<E, T> mapper) {
        PagedResponse<T> response = new PagedResponse<>();
        response.data = page.getContent().stream().map(mapper).toList();

        PaginationMeta meta = new PaginationMeta();
        meta.page  = page.getNumber() + 1;      // 1-based for API consumers
        meta.limit = page.getSize();
        meta.total = page.getTotalElements();
        meta.pages = page.getTotalPages();
        response.pagination = meta;

        return response;
    }
}
