package com.example.reportfrontapi.domain.habit.controller;

import com.example.reportfrontapi.common.response.ApiResponse;
import com.example.reportfrontapi.domain.habit.application.DailyHabitResponse;
import com.example.reportfrontapi.domain.habit.application.MonthlyHabitResponse;
import com.example.reportfrontapi.domain.habit.application.ReportHabitRequest;
import com.example.reportfrontapi.domain.habit.application.ReportHabitResponse;
import com.example.reportfrontapi.domain.habit.application.ReportHabitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/habits")
@RequiredArgsConstructor
public class ReportHabitController {
    private final ReportHabitService reportHabitService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReportHabitResponse> create(@Valid @RequestBody ReportHabitRequest request) {
        return ApiResponse.success(reportHabitService.create(request));
    }

    @GetMapping
    public ApiResponse<List<ReportHabitResponse>> findAll() {
        return ApiResponse.success(reportHabitService.findAll());
    }

    @GetMapping("/month")
    public ApiResponse<MonthlyHabitResponse> findByMonth(@RequestParam int year, @RequestParam int month) {
        return ApiResponse.success(reportHabitService.findByMonth(year, month));
    }

    @GetMapping("/daily")
    public ApiResponse<DailyHabitResponse> findByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.success(reportHabitService.findByDate(date));
    }

    @GetMapping("/{id}")
    public ApiResponse<ReportHabitResponse> findById(@PathVariable Long id) {
        return ApiResponse.success(reportHabitService.findById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<ReportHabitResponse> update(@PathVariable Long id, @Valid @RequestBody ReportHabitRequest request) {
        return ApiResponse.success(reportHabitService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        reportHabitService.delete(id);
        return ApiResponse.success(null);
    }
}
