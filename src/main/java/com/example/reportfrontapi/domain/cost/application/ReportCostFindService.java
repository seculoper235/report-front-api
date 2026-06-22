package com.example.reportfrontapi.domain.cost.application;

import com.example.reportfrontapi.common.response.PageResponse;
import com.example.reportfrontapi.domain.cost.controller.dto.CalendarCostFindResponse;
import com.example.reportfrontapi.domain.cost.controller.dto.CategoryCostFindResponse;
import com.example.reportfrontapi.domain.cost.controller.dto.ReportCostFindResponse;
import com.example.reportfrontapi.domain.cost.controller.dto.WeeklyCostFindResponse;
import com.example.reportfrontapi.domain.cost.model.CostDivision;
import com.example.reportfrontapi.domain.cost.model.ReportCost;
import com.example.reportfrontapi.domain.cost.repository.ReportCostRepository;
import com.example.reportfrontapi.web.security.SecurityUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportCostFindService {
    private final ReportCostRepository reportCostRepository;

    public List<ReportCostFindResponse> findAll(Long categoryId) {
        return reportCostRepository.findByCategoryId(categoryId, SecurityUtil.getRequiredCurrentUserId());
    }

    // 소비 내역 무한 스크롤 조회. division/기간은 옵션, 정렬은 pageable로 받는다.
    public PageResponse<ReportCostFindResponse> search(CostDivision division,
                                                       LocalDate startDate,
                                                       LocalDate endDate,
                                                       Pageable pageable) {
        // 기간은 [startDate 00:00, endDate+1 00:00) 으로 끝 날짜를 포함하도록 처리.
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end = endDate != null ? endDate.plusDays(1).atStartOfDay() : null;

        Long userId = SecurityUtil.getRequiredCurrentUserId();
        List<ReportCostFindResponse> content = reportCostRepository.search(division, start, end, pageable, userId);
        Page<ReportCostFindResponse> page = PageableExecutionUtils.getPage(
                content, pageable, () -> reportCostRepository.countSearch(division, start, end, userId));

        return PageResponse.from(page);
    }

    // 전체 순포인트 합계(GOOD +, BAD -). 메인 화면 헤더에 표시. 집계 대상이 없으면 0.
    public int getTotalPoint() {
        Integer netPoint = reportCostRepository.sumNetPoint(SecurityUtil.getRequiredCurrentUserId());
        return netPoint != null ? netPoint : 0;
    }

    // /calendar : 일별/월별 입금(INCREASE)/출금(DECREASE) costAmount 합산
    public CalendarCostFindResponse findCalendar(int year, int month) {
        List<ReportCost> costs = findByMonthRange(year, month);

        // 일자별 그룹핑(달력 순서 유지를 위해 TreeMap)
        Map<LocalDate, List<ReportCost>> byDate = costs.stream()
                .collect(Collectors.groupingBy(
                        cost -> cost.getPaymentAt().toLocalDate(),
                        TreeMap::new,
                        Collectors.toList()));

        List<CalendarCostFindResponse.DailySummary> daily = byDate.entrySet().stream()
                .map(entry -> new CalendarCostFindResponse.DailySummary(
                        entry.getKey(),
                        sumIncomeAmount(entry.getValue()),
                        sumExpenseAmount(entry.getValue())))
                .toList();

        CalendarCostFindResponse.MonthlySummary monthly = new CalendarCostFindResponse.MonthlySummary(
                year,
                month,
                sumIncomeAmount(costs),
                sumExpenseAmount(costs));

        return new CalendarCostFindResponse(daily, monthly);
    }

    // /week : 해당 월의 ISO-8601(월요일 시작) 주차별 입금/출금 costAmount 합산
    public List<WeeklyCostFindResponse> findWeekly(int year, int month) {
        List<ReportCost> costs = findByMonthRange(year, month);

        // ISO 주의 월요일을 키로 그룹핑(시간순 정렬을 위해 TreeMap)
        Map<LocalDate, List<ReportCost>> byWeek = costs.stream()
                .collect(Collectors.groupingBy(
                        cost -> cost.getPaymentAt().toLocalDate().with(WeekFields.ISO.dayOfWeek(), 1),
                        TreeMap::new,
                        Collectors.toList()));

        return byWeek.entrySet().stream()
                .map(entry -> {
                    LocalDate monday = entry.getKey();
                    return new WeeklyCostFindResponse(
                            monday.get(WeekFields.ISO.weekBasedYear()),
                            monday.get(WeekFields.ISO.weekOfWeekBasedYear()),
                            monday,
                            sumIncomeAmount(entry.getValue()),
                            sumExpenseAmount(entry.getValue()));
                })
                .toList();
    }

    // /month : category별 입금/출금 costAmount 합산 목록
    public List<CategoryCostFindResponse> findMonthlyByCategory(int year, int month) {
        List<ReportCost> costs = findByMonthRange(year, month);

        // 카테고리(RPT_COST_CAT) ID별 그룹핑(ID순 정렬을 위해 TreeMap), 이름은 연관 엔티티에서 가져온다.
        Map<Long, List<ReportCost>> byCategory = costs.stream()
                .collect(Collectors.groupingBy(
                        cost -> cost.getCategory().getCategoryId(),
                        TreeMap::new,
                        Collectors.toList()));

        return byCategory.values().stream()
                .map(group -> new CategoryCostFindResponse(
                        group.get(0).getCategory().getCategoryId(),
                        group.get(0).getCategory().getCategoryName(),
                        group.get(0).getCategory().getColor(),
                        sumIncomeAmount(group),
                        sumExpenseAmount(group)))
                .toList();
    }

    public ReportCostFindResponse findById(Long id) {
        return reportCostRepository.findResponseByIdAndOwner(id, SecurityUtil.getRequiredCurrentUserId())
                .orElseThrow(() -> new EntityNotFoundException("ReportCost not found: " + id));
    }

    private List<ReportCost> findByMonthRange(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

        return reportCostRepository.findByPaymentAtRange(start, end, SecurityUtil.getRequiredCurrentUserId());
    }

    // 입금(INCREASE) 건의 costAmount 합산. getIncomeAmount()는 입금이 아니거나 null이면 0을 반환한다.
    private BigInteger sumIncomeAmount(List<ReportCost> costs) {
        return costs.stream()
                .map(ReportCost::getIncomeAmount)
                .reduce(BigInteger.ZERO, BigInteger::add);
    }

    // 출금(DECREASE) 건의 costAmount 합산. getExpenseAmount()는 출금이 아니거나 null이면 0을 반환한다.
    private BigInteger sumExpenseAmount(List<ReportCost> costs) {
        return costs.stream()
                .map(ReportCost::getExpenseAmount)
                .reduce(BigInteger.ZERO, BigInteger::add);
    }
}
