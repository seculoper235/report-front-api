package com.example.reportfrontapi.domain.cost.application;

import com.example.reportfrontapi.common.response.PageResponse;
import com.example.reportfrontapi.domain.cost.CostDivision;
import com.example.reportfrontapi.domain.cost.ReportCost;
import com.example.reportfrontapi.domain.cost.repository.ReportCostRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .costAmount(request.costAmount())
                .paymentMethod(request.paymentMethod())
                .paymentAt(request.paymentAt())
                .costDivision(request.costDivision())
                .costPoint(request.costPoint())
                .build();

        return ReportCostResponse.from(reportCostRepository.save(cost));
    }

    public List<ReportCostResponse> findAll(String category) {
        return reportCostRepository.findByCategoryName(category).stream()
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

        return PageResponse.from(
                reportCostRepository.search(division, start, end, pageable)
                        .map(ReportCostResponse::from));
    }

    // 전체 순포인트 합계(GOOD +, BAD -). 메인 화면 헤더에 표시.
    public int getTotalPoint() {
        return reportCostRepository.sumNetPoint();
    }

    // /calendar : 일별/월별 GOOD/BAD costPoint 합산 (habit의 month와 동일 구조)
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
                        sumPoints(entry.getValue(), CostDivision.GOOD),
                        sumPoints(entry.getValue(), CostDivision.BAD)))
                .toList();

        CalendarCostResponse.MonthlySummary monthly = new CalendarCostResponse.MonthlySummary(
                year,
                month,
                sumPoints(costs, CostDivision.GOOD),
                sumPoints(costs, CostDivision.BAD));

        return new CalendarCostResponse(daily, monthly);
    }

    // /week : 해당 월의 ISO-8601(월요일 시작) 주차별 GOOD/BAD costPoint 합산
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
                            sumPoints(entry.getValue(), CostDivision.GOOD),
                            sumPoints(entry.getValue(), CostDivision.BAD));
                })
                .toList();
    }

    // /month : category별 totalCostPoint 순합(net) 목록
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
                        entry.getValue().stream().mapToInt(ReportCost::getNormalCostPoint).sum()))
                .toList();
    }

    private List<ReportCost> findByMonthRange(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

        return reportCostRepository.findByPaymentAtRange(start, end);
    }

    // 지정 division의 costPoint 합산. getCostPoint()는 부호 없는 원본 포인트(null이면 0)를 반환한다.
    private int sumPoints(List<ReportCost> costs, CostDivision division) {
        return costs.stream()
                .filter(cost -> cost.getCostDivision() == division)
                .mapToInt(ReportCost::getCostPoint)
                .sum();
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
        return reportCostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ReportCost not found: " + id));
    }
}
