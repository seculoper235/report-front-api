package com.example.reportfrontapi.domain.category.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CostCategoryUpdateRequest(
        @NotBlank
        @Size(max = 20)
        String categoryName    // 변경할 카테고리 이름 (cat_nm, 최대 20자)
) {
}
