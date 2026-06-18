package com.example.reportfrontapi.domain.cost.controller;

import com.example.reportfrontapi.common.response.ApiResponse;
import com.example.reportfrontapi.domain.cost.application.CalendarCostResponse;
import com.example.reportfrontapi.domain.cost.application.CategoryCostResponse;
import com.example.reportfrontapi.domain.cost.application.ReportCostRequest;
import com.example.reportfrontapi.domain.cost.application.ReportCostResponse;
import com.example.reportfrontapi.domain.cost.application.ReportCostService;
import com.example.reportfrontapi.domain.cost.application.WeeklyCostResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/costs")
@RequiredArgsConstructor
public class ReportCostController {
    private final ReportCostService reportCostService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReportCostResponse> create(@Valid @RequestBody ReportCostRequest request) {
        return ApiResponse.success(reportCostService.create(request));
    }

    @GetMapping
    public ApiResponse<List<ReportCostResponse>> findAll(@RequestParam String category) {
        return ApiResponse.success(reportCostService.findAll(category));
    }

    @GetMapping("/calendar")
    public ApiResponse<CalendarCostResponse> findCalendar(@RequestParam int year, @RequestParam int month) {
        return ApiResponse.success(reportCostService.findCalendar(year, month));
    }

    @GetMapping("/week")
    public ApiResponse<List<WeeklyCostResponse>> findWeekly(@RequestParam int year, @RequestParam int month) {
        return ApiResponse.success(reportCostService.findWeekly(year, month));
    }

    @GetMapping("/month")
    public ApiResponse<List<CategoryCostResponse>> findMonthlyByCategory(@RequestParam int year, @RequestParam int month) {
        return ApiResponse.success(reportCostService.findMonthlyByCategory(year, month));
    }

    @GetMapping("/{id}")
    public ApiResponse<ReportCostResponse> findById(@PathVariable Long id) {
        return ApiResponse.success(reportCostService.findById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<ReportCostResponse> update(@PathVariable Long id, @Valid @RequestBody ReportCostRequest request) {
        return ApiResponse.success(reportCostService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        reportCostService.delete(id);
        return ApiResponse.success(null);
    }
}
