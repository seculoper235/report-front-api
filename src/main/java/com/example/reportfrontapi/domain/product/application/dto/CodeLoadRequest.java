package com.example.reportfrontapi.domain.product.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;
import java.util.List;

public record CodeLoadRequest(
        @NotEmpty @Valid List<CodeItem> codes
) {
    public record CodeItem(
            @NotBlank String code,
            String barcodeImageUrl,
            LocalDate validUntil
    ) {
    }
}
