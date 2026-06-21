package com.example.reportfrontapi.domain.category.controller.dto;

/**
 * 지출 카테고리(CostCategory) 조회 응답(목록 요소).
 * 등록/수정 화면의 카테고리 선택 및 카테고리 상세 진입에 사용한다.
 */
public record CostCategoryFindResponse(
        Long categoryId,       // 카테고리 ID
        String categoryName    // 카테고리 이름
) {
}
