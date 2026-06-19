package com.example.reportfrontapi.domain.redemption.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RedeemRequest(
        @NotNull Long productId,
        @NotBlank String idempotencyKey
) {
}
