package com.example.reportfrontapi.domain.cost.controller;

import com.example.reportfrontapi.common.response.ApiResponse;
import com.example.reportfrontapi.common.response.PageResponse;
import com.example.reportfrontapi.domain.cost.application.ReportCostCreateService;
import com.example.reportfrontapi.domain.cost.application.ReportCostDeleteService;
import com.example.reportfrontapi.domain.cost.application.ReportCostFindService;
import com.example.reportfrontapi.domain.cost.controller.dto.CalendarCostFindResponse;
import com.example.reportfrontapi.domain.cost.controller.dto.CategoryCostFindResponse;
import com.example.reportfrontapi.domain.cost.controller.dto.ReportCostCreateRequest;
import com.example.reportfrontapi.domain.cost.controller.dto.ReportCostCreateResponse;
import com.example.reportfrontapi.domain.cost.controller.dto.ReportCostFindResponse;
import com.example.reportfrontapi.domain.cost.controller.dto.WeeklyCostFindResponse;
import com.example.reportfrontapi.domain.cost.model.CostDivision;
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
    private final ReportCostFindService reportCostFindService;
    private final ReportCostCreateService reportCostCreateService;
    private final ReportCostDeleteService reportCostDeleteService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReportCostCreateResponse> create(@Valid @RequestBody ReportCostCreateRequest request) {
        return ApiResponse.success(reportCostCreateService.create(request));
    }

    @GetMapping
    public ApiResponse<List<ReportCostFindResponse>> findAll(@RequestParam Long categoryId) {
        return ApiResponse.success(reportCostFindService.findAll(categoryId));
    }

    // 소비 내역 무한 스크롤 조회.
    // 예) /api/costs/search?page=0&size=15&sort=paymentAt,desc&division=GOOD&startDate=2026-05-01&endDate=2026-06-19
    // division/startDate/endDate 는 옵션(미지정 시 전체). 정렬 가능 필드: paymentAt, costPoint, costAmount
    @GetMapping("/search")
    public ApiResponse<PageResponse<ReportCostFindResponse>> search(
            @RequestParam(required = false) CostDivision division,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 15, sort = "paymentAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.success(reportCostFindService.search(division, startDate, endDate, pageable));
    }

    // 전체 순포인트 합계(메인 화면 헤더). GOOD +, BAD -
    @GetMapping("/point")
    public ApiResponse<Integer> getTotalPoint() {
        return ApiResponse.success(reportCostFindService.getTotalPoint());
    }

    @GetMapping("/calendar")
    public ApiResponse<CalendarCostFindResponse> findCalendar(@RequestParam int year, @RequestParam int month) {
        return ApiResponse.success(reportCostFindService.findCalendar(year, month));
    }

    @GetMapping("/week")
    public ApiResponse<List<WeeklyCostFindResponse>> findWeekly(@RequestParam int year, @RequestParam int month) {
        return ApiResponse.success(reportCostFindService.findWeekly(year, month));
    }

    @GetMapping("/month")
    public ApiResponse<List<CategoryCostFindResponse>> findMonthlyByCategory(@RequestParam int year, @RequestParam int month) {
        return ApiResponse.success(reportCostFindService.findMonthlyByCategory(year, month));
    }

    @GetMapping("/{id}")
    public ApiResponse<ReportCostFindResponse> findById(@PathVariable Long id) {
        return ApiResponse.success(reportCostFindService.findById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<ReportCostCreateResponse> update(@PathVariable Long id, @Valid @RequestBody ReportCostCreateRequest request) {
        return ApiResponse.success(reportCostCreateService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        reportCostDeleteService.delete(id);
        return ApiResponse.success(null);
    }
}
