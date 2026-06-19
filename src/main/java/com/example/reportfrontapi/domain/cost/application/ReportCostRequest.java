package com.example.reportfrontapi.domain.cost.application;

import com.example.reportfrontapi.common.dto.Yn;
import com.example.reportfrontapi.domain.cost.CostAmountDivision;
import com.example.reportfrontapi.domain.cost.CostDivision;
import com.example.reportfrontapi.domain.cost.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigInteger;
import java.time.LocalDateTime;

public record ReportCostRequest(
        @NotBlank
        String categoryName,         // 카테고리 이름
        @NotBlank
        String costName,             // 코스트 이름
        @NotNull
        Yn fixedYn,                  // 고정 지출 여부
        String costDescription,      // 코스트 상세
        @NotNull
        CostAmountDivision amountDivision, // 입금/출금 구분
        @NotNull
        BigInteger costAmount,       // 코스트 금액
        @NotNull
        PaymentMethod paymentMethod, // 지출 수단
        @NotNull
        LocalDateTime paymentAt,     // 지출 일시
        CostDivision costDivision,   // 소비 유형
        Integer costPoint            // 소비 포인트
) {
}
