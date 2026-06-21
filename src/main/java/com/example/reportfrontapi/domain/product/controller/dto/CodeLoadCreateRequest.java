package com.example.reportfrontapi.domain.product.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;
import java.util.List;

public record CodeLoadCreateRequest(
        @NotEmpty @Valid List<CodeItem> codes
) {
    public record CodeItem(
            @NotBlank String code,
            // 비공개 바코드 이미지의 S3 object key. /api/admin/uploads(BARCODE_IMAGE)로 업로드 후
            // 받은 objectKey를 전달. 조회 시 소유자에게만 presigned GET URL로 변환되어 노출된다.
            String barcodeImageUrl,
            LocalDate validUntil
    ) {
    }
}
