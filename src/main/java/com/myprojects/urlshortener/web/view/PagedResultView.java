package com.myprojects.urlshortener.web.view;

import org.springframework.data.domain.Page;

import java.util.List;

public record PagedResultView<T>(
        List<T> data,
        int pageNumber,
        int totalPages,
        long totalElements,
        boolean isFirst,
        boolean isLast,
        boolean hasNext,
        boolean hasPrevious) {

    public static <T> PagedResultView<T> from(Page<T> page) {
        return new PagedResultView<>(
                page.getContent(),
                page.getNumber() + 1, //to show 1-based page numbering
                page.getTotalPages(),
                page.getTotalElements(),
                page.isFirst(),
                page.isLast(),
                page.hasNext(),
                page.hasPrevious());
    }
}