package com.example.reportfrontapi.common.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
        List<T> content,       // 현재 페이지 데이터
        int page,              // 현재 페이지 번호(0-base)
        int size,              // 페이지 크기
        long totalElements,    // 전체 건수
        int totalPages,        // 전체 페이지 수
        boolean hasNext        // 다음 페이지 존재 여부(무한 스크롤용)
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}
