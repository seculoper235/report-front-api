package com.example.reportfrontapi.domain.habit.controller;

import com.example.reportfrontapi.common.response.ApiResponse;
import com.example.reportfrontapi.domain.habit.application.ReportHabitRequest;
import com.example.reportfrontapi.domain.habit.application.ReportHabitResponse;
import com.example.reportfrontapi.domain.habit.application.ReportHabitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
