package com.example.reportfrontapi.domain.cost.controller;

import com.example.reportfrontapi.common.response.ApiResponse;
import com.example.reportfrontapi.common.response.PageResponse;
import com.example.reportfrontapi.domain.cost.CostDivision;
import com.example.reportfrontapi.domain.cost.application.CalendarCostResponse;
import com.example.reportfrontapi.domain.cost.application.CategoryCostResponse;
import com.example.reportfrontapi.domain.cost.application.ReportCostRequest;
import com.example.reportfrontapi.domain.cost.application.ReportCostResponse;
import com.example.reportfrontapi.domain.cost.application.ReportCostService;
import com.example.reportfrontapi.domain.cost.application.WeeklyCostResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
    public ApiResponse<List<ReportCostResponse>> findAll(@RequestParam Long categoryId) {
        return ApiResponse.success(reportCostService.findAll(categoryId));
    }

    // 소비 내역 무한 스크롤 조회.
    // 예) /api/costs/search?page=0&size=15&sort=paymentAt,desc&division=GOOD&startDate=2026-05-01&endDate=2026-06-19
    // division/startDate/endDate 는 옵션(미지정 시 전체). 정렬 가능 필드: paymentAt, costPoint, costAmount
    @GetMapping("/search")
    public ApiResponse<PageResponse<ReportCostResponse>> search(
            @RequestParam(required = false) CostDivision division,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 15, sort = "paymentAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.success(reportCostService.search(division, startDate, endDate, pageable));
    }

    // 전체 순포인트 합계(메인 화면 헤더). GOOD +, BAD -
    @GetMapping("/point")
    public ApiResponse<Integer> getTotalPoint() {
        return ApiResponse.success(reportCostService.getTotalPoint());
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
