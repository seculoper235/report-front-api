package com.example.reportfrontapi.domain.habit.controller;

import com.example.reportfrontapi.common.response.ApiResponse;
import com.example.reportfrontapi.domain.habit.application.ReportHabitCreateService;
import com.example.reportfrontapi.domain.habit.application.ReportHabitDeleteService;
import com.example.reportfrontapi.domain.habit.application.ReportHabitFindService;
import com.example.reportfrontapi.domain.habit.controller.dto.DailyHabitFindResponse;
import com.example.reportfrontapi.domain.habit.controller.dto.MonthlyHabitFindResponse;
import com.example.reportfrontapi.domain.habit.controller.dto.ReportHabitCreateRequest;
import com.example.reportfrontapi.domain.habit.controller.dto.ReportHabitCreateResponse;
import com.example.reportfrontapi.domain.habit.controller.dto.ReportHabitFindResponse;
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
    private final ReportHabitFindService reportHabitFindService;
    private final ReportHabitCreateService reportHabitCreateService;
    private final ReportHabitDeleteService reportHabitDeleteService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReportHabitCreateResponse> create(@Valid @RequestBody ReportHabitCreateRequest request) {
        return ApiResponse.success(reportHabitCreateService.create(request));
    }

    @GetMapping
    public ApiResponse<List<ReportHabitFindResponse>> findAll() {
        return ApiResponse.success(reportHabitFindService.findAll());
    }

    @GetMapping("/month")
    public ApiResponse<MonthlyHabitFindResponse> findByMonth(@RequestParam int year, @RequestParam int month) {
        return ApiResponse.success(reportHabitFindService.findByMonth(year, month));
    }

    @GetMapping("/daily")
    public ApiResponse<DailyHabitFindResponse> findByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.success(reportHabitFindService.findByDate(date));
    }

    @GetMapping("/{id}")
    public ApiResponse<ReportHabitFindResponse> findById(@PathVariable Long id) {
        return ApiResponse.success(reportHabitFindService.findById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<ReportHabitCreateResponse> update(@PathVariable Long id, @Valid @RequestBody ReportHabitCreateRequest request) {
        return ApiResponse.success(reportHabitCreateService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        reportHabitDeleteService.delete(id);
        return ApiResponse.success(null);
    }
}
