package com.example.reportfrontapi.domain.product.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProductCreateRequest(
        @NotBlank String name,
        String brand,
        // 공개 썸네일 URL. /api/admin/uploads(PRODUCT_IMAGE) 업로드 후 받은 publicUrl을 전달.
        String imageUrl,
        @NotNull @Positive Integer pointCost
) {
}
