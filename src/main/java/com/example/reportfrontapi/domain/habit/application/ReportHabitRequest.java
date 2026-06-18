package com.example.reportfrontapi.domain.habit.application;

import com.example.reportfrontapi.domain.habit.HabitDivision;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReportHabitRequest(
        @NotBlank
        String habitName,        // 습관 이름
        @NotNull
        HabitDivision habitDivision,  // 습관 유형
        @NotNull
        Integer habitPoint       // 습관 포인트
) {
}
