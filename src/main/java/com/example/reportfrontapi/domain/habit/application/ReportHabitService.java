package com.example.reportfrontapi.domain.habit.application;

import com.example.reportfrontapi.domain.habit.HabitDivision;
import com.example.reportfrontapi.domain.habit.ReportHabit;
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
public class ReportHabitService {
    private final ReportHabitRepository reportHabitRepository;

    @Transactional
    public ReportHabitResponse create(ReportHabitRequest request) {
        ReportHabit habit = new ReportHabit();
        habit.setUserId(SecurityUtil.getRequiredCurrentUserId());
        habit.setHabitName(request.habitName());
        habit.setHabitDivision(request.habitDivision());
        habit.setHabitPoint(request.habitPoint());

        return ReportHabitResponse.from(reportHabitRepository.save(habit));
    }

    public List<ReportHabitResponse> findAll() {
        return reportHabitRepository.findAllByOwner(SecurityUtil.getRequiredCurrentUserId()).stream()
                .map(ReportHabitResponse::from)
                .toList();
    }

    public MonthlyHabitResponse findByMonth(int year, int month) {
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

        List<MonthlyHabitResponse.DailySummary> daily = byDate.entrySet().stream()
                .map(entry -> new MonthlyHabitResponse.DailySummary(
                        entry.getKey(),
                        sumPoints(entry.getValue(), HabitDivision.GOOD),
                        sumPoints(entry.getValue(), HabitDivision.BAD)))
                .toList();

        MonthlyHabitResponse.MonthlySummary monthly = new MonthlyHabitResponse.MonthlySummary(
                year,
                month,
                sumPoints(habits, HabitDivision.GOOD),
                sumPoints(habits, HabitDivision.BAD));

        return new MonthlyHabitResponse(daily, monthly);
    }

    public DailyHabitResponse findByDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<ReportHabit> habits =
                reportHabitRepository.findByCreatedAtRange(start, end, SecurityUtil.getRequiredCurrentUserId());

        List<ReportHabitResponse> habitResponses = habits.stream()
                .map(ReportHabitResponse::from)
                .toList();

        return new DailyHabitResponse(
                date,
                habitResponses,
                sumPoints(habits, HabitDivision.GOOD),
                sumPoints(habits, HabitDivision.BAD));
    }

    // 지정 division의 habitPoint 합산. getHabitPoint()는 부호 없는 원본 포인트(null이면 0)를 반환한다.
    private int sumPoints(List<ReportHabit> habits, HabitDivision division) {
        return habits.stream()
                .filter(habit -> habit.getHabitDivision() == division)
                .mapToInt(ReportHabit::getHabitPoint)
                .sum();
    }

    public ReportHabitResponse findById(Long id) {
        return ReportHabitResponse.from(getOrThrow(id));
    }

    @Transactional
    public ReportHabitResponse update(Long id, ReportHabitRequest request) {
        ReportHabit habit = getOrThrow(id);
        habit.update(
                request.habitName(),
                request.habitDivision(),
                request.habitPoint()
        );

        return ReportHabitResponse.from(habit);
    }

    @Transactional
    public void delete(Long id) {
        ReportHabit habit = getOrThrow(id);
        reportHabitRepository.delete(habit);
    }

    private ReportHabit getOrThrow(Long id) {
        return reportHabitRepository.findByIdAndOwner(id, SecurityUtil.getRequiredCurrentUserId())
                .orElseThrow(() -> new EntityNotFoundException("ReportHabit not found: " + id));
    }
}
