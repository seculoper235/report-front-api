package com.example.reportfrontapi.common.storage.dto;

import com.example.reportfrontapi.common.storage.UploadType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UploadUrlRequest(
        @NotNull UploadType type,
        @NotBlank String contentType
) {
}
