package com.example.reportfrontapi.domain.redemption.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record RedemptionResponse(
        Long orderId,
        Long productId,
        String productName,
        Integer pointCost,
        String status,
        String code,                // 지급된 쿠폰코드/핀번호
        String barcodeImageUrl,
        LocalDate validUntil,
        LocalDateTime createdAt
) {
}
