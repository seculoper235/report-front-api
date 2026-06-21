package com.example.reportfrontapi.domain.redemption.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RedeemCreateRequest(
        @NotNull Long productId,
        @NotBlank String idempotencyKey
) {
}
