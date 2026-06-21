package com.example.reportfrontapi.domain.habit.controller.dto;

import com.example.reportfrontapi.domain.habit.model.HabitDivision;
import com.example.reportfrontapi.domain.habit.model.ReportHabit;

public record ReportHabitFindResponse(
        Long reportHabitId,      // 레포트 습관 일련번호
        String habitName,        // 습관 이름
        HabitDivision habitDivision,  // 습관 유형
        Integer habitPoint       // 습관 포인트(유형에 따른 부호 적용)
) {
    public static ReportHabitFindResponse from(ReportHabit habit) {
        return new ReportHabitFindResponse(
                habit.getReportCostId(),
                habit.getHabitName(),
                habit.getHabitDivision(),
                habit.getHabitPoint()
        );
    }
}
