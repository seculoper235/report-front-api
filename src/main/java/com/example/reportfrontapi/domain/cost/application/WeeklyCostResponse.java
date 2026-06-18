package com.example.reportfrontapi.domain.cost.application;

import java.time.LocalDate;

/**
 * 주별 소비 집계 응답(목록 요소). paymentAt 기준, ISO-8601(월요일 시작) 주차.
 * goodPoint/badPoint는 각 division에 해당하는 costPoint 원본 값을 합산한 값(둘 다 양수)이다.
 */
public record WeeklyCostResponse(
        int year,            // 주 기준 연도(ISO weekBasedYear)
        int week,            // ISO 주차
        LocalDate startDate, // 해당 주의 월요일
        int goodPoint,       // 주별 GOOD costPoint 합산
        int badPoint         // 주별 BAD costPoint 합산
) {
    public int getTotalPoint() {
        return goodPoint - badPoint;
    }
}
