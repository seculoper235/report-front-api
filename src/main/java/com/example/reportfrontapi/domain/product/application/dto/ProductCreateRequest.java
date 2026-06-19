package com.example.reportfrontapi.domain.product.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProductCreateRequest(
        @NotBlank String name,
        String brand,
        String imageUrl,
        @NotNull @Positive Integer pointCost
) {
}
