package com.example.reportfrontapi.domain.habit.application;

import java.time.LocalDate;
import java.util.List;

/**
 * 일별 습관 조회 응답.
 * goodPoint/badPoint는 각 division에 해당하는 habitPoint 원본 값을 합산한 값(둘 다 양수)이다.
 */
public record DailyHabitResponse(
        LocalDate date,                      // 일자
        List<ReportHabitResponse> habits,    // 해당 일자의 습관 목록
        int goodPoint,                       // GOOD habitPoint 합산
        int badPoint                         // BAD habitPoint 합산
) {
    public int getTotalPoint() {
        return goodPoint - badPoint;
    }
}
