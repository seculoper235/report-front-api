package com.example.reportfrontapi.domain.habit.controller.dto;

import com.example.reportfrontapi.domain.habit.model.HabitDivision;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReportHabitCreateRequest(
        @NotBlank
        String habitName,        // 습관 이름
        @NotNull
        HabitDivision habitDivision,  // 습관 유형
        @NotNull
        Integer habitPoint       // 습관 포인트
) {
}
