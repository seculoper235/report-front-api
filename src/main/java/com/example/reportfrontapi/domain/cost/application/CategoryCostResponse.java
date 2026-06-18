package com.example.reportfrontapi.domain.cost.application;

/**
 * category별 소비 집계 응답(목록 요소). paymentAt 기준.
 * totalCostPoint는 getNormalCostPoint(GOOD +, BAD -)의 순합(net)이다.
 */
public record CategoryCostResponse(
        String categoryName,   // 카테고리 이름
        int totalCostPoint     // category별 costPoint 순합(net)
) {
}
