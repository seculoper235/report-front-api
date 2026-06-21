package com.example.reportfrontapi.domain.habit.application;

import com.example.reportfrontapi.domain.habit.controller.dto.DailyHabitFindResponse;
import com.example.reportfrontapi.domain.habit.controller.dto.MonthlyHabitFindResponse;
import com.example.reportfrontapi.domain.habit.controller.dto.ReportHabitFindResponse;
import com.example.reportfrontapi.domain.habit.model.HabitDivision;
import com.example.reportfrontapi.domain.habit.model.ReportHabit;
import com.example.reportfrontapi.domain.habit.repository.ReportHabitRepository;
import com.example.reportfrontapi.web.security.SecurityUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportHabitFindService {
    private final ReportHabitRepository reportHabitRepository;

    public List<ReportHabitFindResponse> findAll() {
        return reportHabitRepository.findAllByOwner(SecurityUtil.getRequiredCurrentUserId()).stream()
                .map(ReportHabitFindResponse::from)
                .toList();
    }

    public MonthlyHabitFindResponse findByMonth(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

        List<ReportHabit> habits =
                reportHabitRepository.findByCreatedAtRange(start, end, SecurityUtil.getRequiredCurrentUserId());

        // 일자별 그룹핑(달력 순서 유지를 위해 TreeMap)
        Map<LocalDate, List<ReportHabit>> byDate = habits.stream()
                .collect(Collectors.groupingBy(
                        habit -> habit.getCreatedAt().toLocalDate(),
                        TreeMap::new,
                        Collectors.toList()));

        List<MonthlyHabitFindResponse.DailySummary> daily = byDate.entrySet().stream()
                .map(entry -> new MonthlyHabitFindResponse.DailySummary(
                        entry.getKey(),
                        sumPoints(entry.getValue(), HabitDivision.GOOD),
                        sumPoints(entry.getValue(), HabitDivision.BAD)))
                .toList();

        MonthlyHabitFindResponse.MonthlySummary monthly = new MonthlyHabitFindResponse.MonthlySummary(
                year,
                month,
                sumPoints(habits, HabitDivision.GOOD),
                sumPoints(habits, HabitDivision.BAD));

        return new MonthlyHabitFindResponse(daily, monthly);
    }

    public DailyHabitFindResponse findByDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<ReportHabit> habits =
                reportHabitRepository.findByCreatedAtRange(start, end, SecurityUtil.getRequiredCurrentUserId());

        List<ReportHabitFindResponse> habitResponses = habits.stream()
                .map(ReportHabitFindResponse::from)
                .toList();

        return new DailyHabitFindResponse(
                date,
                habitResponses,
                sumPoints(habits, HabitDivision.GOOD),
                sumPoints(habits, HabitDivision.BAD));
    }

    public ReportHabitFindResponse findById(Long id) {
        return ReportHabitFindResponse.from(getOrThrow(id));
    }

    // 지정 division의 habitPoint 합산. getHabitPoint()는 부호 없는 원본 포인트(null이면 0)를 반환한다.
    private int sumPoints(List<ReportHabit> habits, HabitDivision division) {
        return habits.stream()
                .filter(habit -> habit.getHabitDivision() == division)
                .mapToInt(ReportHabit::getHabitPoint)
                .sum();
    }

    private ReportHabit getOrThrow(Long id) {
        return reportHabitRepository.findByIdAndOwner(id, SecurityUtil.getRequiredCurrentUserId())
                .orElseThrow(() -> new EntityNotFoundException("ReportHabit not found: " + id));
    }
}
