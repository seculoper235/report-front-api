package com.example.reportfrontapi.domain.cost.application;

import com.example.reportfrontapi.common.dto.Yn;
import com.example.reportfrontapi.domain.cost.CostAmountDivision;
import com.example.reportfrontapi.domain.cost.CostDivision;
import com.example.reportfrontapi.domain.cost.PaymentMethod;
import com.example.reportfrontapi.domain.cost.ReportCost;

import java.math.BigInteger;
import java.time.LocalDateTime;

public record ReportCostResponse(
        Long reportCostId,           // 레포트 코스트 일련번호
        Long categoryId,             // 카테고리 ID
        String categoryName,         // 카테고리 이름
        String costName,             // 코스트 이름
        Yn fixedYn,                  // 고정 지출 여부
        String costDescription,      // 코스트 상세
        CostAmountDivision amountDivision, // 입금/출금 구분
        BigInteger costAmount,       // 코스트 금액
        PaymentMethod paymentMethod, // 지출 수단
        LocalDateTime paymentAt,     // 지출 일시
        CostDivision costDivision,   // 소비 유형
        Integer costPoint            // 소비 포인트(부호 없는 원본 값)
) {
    public static ReportCostResponse from(ReportCost cost) {
        return new ReportCostResponse(
                cost.getReportCostId(),
                cost.getCategoryId(),
                cost.getCategoryName(),
                cost.getCostName(),
                cost.getFixedYn(),
                cost.getCostDescription(),
                cost.getAmountDivision(),
                cost.getCostAmount(),
                cost.getPaymentMethod(),
                cost.getPaymentAt(),
                cost.getCostDivision(),
                cost.getCostPoint()
        );
    }
}
