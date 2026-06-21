package com.example.reportfrontapi.domain.cost.controller.dto;

import com.example.reportfrontapi.common.dto.Yn;
import com.example.reportfrontapi.domain.cost.model.CostAmountDivision;
import com.example.reportfrontapi.domain.cost.model.CostDivision;
import com.example.reportfrontapi.domain.cost.model.PaymentMethod;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * 소비 조회 응답. repository 에서 category innerJoin 프로젝션으로 직접 생성된다(QueryDSL Projections.constructor).
 */
public record ReportCostFindResponse(
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
}
