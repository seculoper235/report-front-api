package com.example.reportfrontapi.domain.habit.application;

import com.example.reportfrontapi.domain.habit.HabitDivision;
import com.example.reportfrontapi.domain.habit.ReportHabit;

public record ReportHabitResponse(
        Long reportHabitId,      // 레포트 습관 일련번호
        String habitName,        // 습관 이름
        HabitDivision habitDivision,  // 습관 유형
        Integer habitPoint       // 습관 포인트(유형에 따른 부호 적용)
) {
    public static ReportHabitResponse from(ReportHabit habit) {
        return new ReportHabitResponse(
                habit.getReportCostId(),
                habit.getHabitName(),
                habit.getHabitDivision(),
                habit.getHabitPoint()
        );
    }
}
