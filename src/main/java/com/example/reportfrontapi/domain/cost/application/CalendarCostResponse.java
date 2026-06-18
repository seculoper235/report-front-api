package com.example.reportfrontapi.domain.cost.application;

import java.time.LocalDate;
import java.util.List;

/**
 * 캘린더(월) 소비 집계 응답. paymentAt 기준.
 * goodPoint/badPoint는 각 division에 해당하는 costPoint 원본 값을 합산한 값(둘 다 양수)이다.
 */
public record CalendarCostResponse(
        List<DailySummary> daily,    // 일별 정보
        MonthlySummary monthly       // 월별 정보
) {
    public record DailySummary(
            LocalDate date,          // 일자
            int goodPoint,           // 일별 GOOD costPoint 합산
            int badPoint             // 일별 BAD costPoint 합산
    ) {
    }

    public record MonthlySummary(
            int year,                // 년
            int month,               // 월
            int goodPoint,           // 월별 GOOD costPoint 합산
            int badPoint             // 월별 BAD costPoint 합산
    ) {
    }
}
