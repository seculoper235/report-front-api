package com.example.reportfrontapi.domain.cost.application;

import com.example.reportfrontapi.common.response.PageResponse;
import com.example.reportfrontapi.domain.cost.CostDivision;
import com.example.reportfrontapi.domain.cost.ReportCost;
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
public class ReportCostService {
    private final ReportCostRepository reportCostRepository;

    @Transactional
    public ReportCostResponse create(ReportCostRequest request) {
        ReportCost cost = ReportCost.builder()
                .categoryName(request.categoryName())
                .costName(request.costName())
                .fixedYn(request.fixedYn())
                .costDescription(request.costDescription())
                .amountDivision(request.amountDivision())
                .costAmount(request.costAmount())
                .paymentMethod(request.paymentMethod())
                .paymentAt(request.paymentAt())
                .costDivision(request.costDivision())
                .costPoint(request.costPoint())
                .build();

        return ReportCostResponse.from(reportCostRepository.save(cost));
    }

    public List<ReportCostResponse> findAll(String category) {
        return reportCostRepository.findByCategoryName(category, SecurityUtil.getRequiredCurrentUserId()).stream()
                .map(ReportCostResponse::from)
                .toList();
    }

    // 소비 내역 무한 스크롤 조회. division/기간은 옵션, 정렬은 pageable로 받는다.
    public PageResponse<ReportCostResponse> search(CostDivision division,
                                                   LocalDate startDate,
                                                   LocalDate endDate,
                                                   Pageable pageable) {
        // 기간은 [startDate 00:00, endDate+1 00:00) 으로 끝 날짜를 포함하도록 처리.
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end = endDate != null ? endDate.plusDays(1).atStartOfDay() : null;

        Long userId = SecurityUtil.getRequiredCurrentUserId();
        List<ReportCost> content = reportCostRepository.search(division, start, end, pageable, userId);
        Page<ReportCost> page = PageableExecutionUtils.getPage(
                content, pageable, () -> reportCostRepository.countSearch(division, start, end, userId));

        return PageResponse.from(page.map(ReportCostResponse::from));
    }

    // 전체 순포인트 합계(GOOD +, BAD -). 메인 화면 헤더에 표시. 집계 대상이 없으면 0.
    public int getTotalPoint() {
        Integer netPoint = reportCostRepository.sumNetPoint(SecurityUtil.getRequiredCurrentUserId());
        return netPoint != null ? netPoint : 0;
    }

    // /calendar : 일별/월별 입금(INCREASE)/출금(DECREASE) costAmount 합산
    public CalendarCostResponse findCalendar(int year, int month) {
        List<ReportCost> costs = findByMonthRange(year, month);

        // 일자별 그룹핑(달력 순서 유지를 위해 TreeMap)
        Map<LocalDate, List<ReportCost>> byDate = costs.stream()
                .collect(Collectors.groupingBy(
                        cost -> cost.getPaymentAt().toLocalDate(),
                        TreeMap::new,
                        Collectors.toList()));

        List<CalendarCostResponse.DailySummary> daily = byDate.entrySet().stream()
                .map(entry -> new CalendarCostResponse.DailySummary(
                        entry.getKey(),
                        sumIncomeAmount(entry.getValue()),
                        sumExpenseAmount(entry.getValue())))
                .toList();

        CalendarCostResponse.MonthlySummary monthly = new CalendarCostResponse.MonthlySummary(
                year,
                month,
                sumIncomeAmount(costs),
                sumExpenseAmount(costs));

        return new CalendarCostResponse(daily, monthly);
    }

    // /week : 해당 월의 ISO-8601(월요일 시작) 주차별 입금/출금 costAmount 합산
    public List<WeeklyCostResponse> findWeekly(int year, int month) {
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
                    return new WeeklyCostResponse(
                            monday.get(WeekFields.ISO.weekBasedYear()),
                            monday.get(WeekFields.ISO.weekOfWeekBasedYear()),
                            monday,
                            sumIncomeAmount(entry.getValue()),
                            sumExpenseAmount(entry.getValue()));
                })
                .toList();
    }

    // /month : category별 입금/출금 costAmount 합산 목록
    public List<CategoryCostResponse> findMonthlyByCategory(int year, int month) {
        List<ReportCost> costs = findByMonthRange(year, month);

        // 카테고리별 그룹핑(이름순 정렬을 위해 TreeMap)
        Map<String, List<ReportCost>> byCategory = costs.stream()
                .collect(Collectors.groupingBy(
                        ReportCost::getCategoryName,
                        TreeMap::new,
                        Collectors.toList()));

        return byCategory.entrySet().stream()
                .map(entry -> new CategoryCostResponse(
                        entry.getKey(),
                        sumIncomeAmount(entry.getValue()),
                        sumExpenseAmount(entry.getValue())))
                .toList();
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

    public ReportCostResponse findById(Long id) {
        return ReportCostResponse.from(getOrThrow(id));
    }

    @Transactional
    public ReportCostResponse update(Long id, ReportCostRequest request) {
        ReportCost cost = getOrThrow(id);
        cost.update(
                request.categoryName(),
                request.costName(),
                request.fixedYn(),
                request.costDescription(),
                request.amountDivision(),
                request.costAmount(),
                request.paymentMethod(),
                request.paymentAt(),
                request.costDivision(),
                request.costPoint()
        );

        return ReportCostResponse.from(cost);
    }

    @Transactional
    public void delete(Long id) {
        ReportCost cost = getOrThrow(id);
        reportCostRepository.delete(cost);
    }

    private ReportCost getOrThrow(Long id) {
        return reportCostRepository.findByIdAndOwner(id, SecurityUtil.getRequiredCurrentUserId())
                .orElseThrow(() -> new EntityNotFoundException("ReportCost not found: " + id));
    }
}
