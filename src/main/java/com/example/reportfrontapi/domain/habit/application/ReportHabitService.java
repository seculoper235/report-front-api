package com.example.reportfrontapi.domain.habit.application;

import com.example.reportfrontapi.domain.habit.ReportHabit;
import com.example.reportfrontapi.domain.habit.repository.ReportHabitRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportHabitService {
    private final ReportHabitRepository reportHabitRepository;

    @Transactional
    public ReportHabitResponse create(ReportHabitRequest request) {
        ReportHabit habit = new ReportHabit();
        habit.setHabitName(request.habitName());
        habit.setHabitDivision(request.habitDivision());
        habit.setHabitPoint(request.habitPoint());

        return ReportHabitResponse.from(reportHabitRepository.save(habit));
    }

    public List<ReportHabitResponse> findAll() {
        return reportHabitRepository.findAll().stream()
                .map(ReportHabitResponse::from)
                .toList();
    }

    public ReportHabitResponse findById(Long id) {
        return ReportHabitResponse.from(getOrThrow(id));
    }

    @Transactional
    public ReportHabitResponse update(Long id, ReportHabitRequest request) {
        ReportHabit habit = getOrThrow(id);
        habit.setHabitName(request.habitName());
        habit.setHabitDivision(request.habitDivision());
        habit.setHabitPoint(request.habitPoint());

        return ReportHabitResponse.from(habit);
    }

    @Transactional
    public void delete(Long id) {
        ReportHabit habit = getOrThrow(id);
        reportHabitRepository.delete(habit);
    }

    private ReportHabit getOrThrow(Long id) {
        return reportHabitRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ReportHabit not found: " + id));
    }
}
