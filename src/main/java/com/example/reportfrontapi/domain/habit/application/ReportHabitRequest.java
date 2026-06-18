package com.example.reportfrontapi.domain.habit.application;

import com.example.reportfrontapi.domain.habit.HabitDivision;

public record ReportHabitRequest(
        String habitName,        // 습관 이름
        HabitDivision habitDivision,  // 습관 유형
        Integer habitPoint       // 습관 포인트
) {
}
