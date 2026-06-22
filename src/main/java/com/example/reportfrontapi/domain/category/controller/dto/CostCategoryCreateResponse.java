package com.example.reportfrontapi.domain.category.controller.dto;

import com.example.reportfrontapi.domain.category.model.CostCategory;

public record CostCategoryCreateResponse(
        Long categoryId,       // 카테고리 ID
        String categoryName    // 카테고리 이름
) {
    public static CostCategoryCreateResponse from(CostCategory category) {
        return new CostCategoryCreateResponse(category.getCategoryId(), category.getCategoryName());
    }
}
