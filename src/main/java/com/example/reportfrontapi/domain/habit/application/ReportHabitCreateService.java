package com.example.reportfrontapi.domain.habit.application;

import com.example.reportfrontapi.domain.habit.controller.dto.ReportHabitCreateRequest;
import com.example.reportfrontapi.domain.habit.controller.dto.ReportHabitCreateResponse;
import com.example.reportfrontapi.domain.habit.model.ReportHabit;
import com.example.reportfrontapi.domain.habit.repository.ReportHabitRepository;
import com.example.reportfrontapi.web.security.SecurityUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportHabitCreateService {
    private final ReportHabitRepository reportHabitRepository;

    @Transactional
    public ReportHabitCreateResponse create(ReportHabitCreateRequest request) {
        ReportHabit habit = new ReportHabit();
        habit.setUserId(SecurityUtil.getRequiredCurrentUserId());
        habit.setHabitName(request.habitName());
        habit.setHabitDivision(request.habitDivision());
        habit.setHabitPoint(request.habitPoint());

        return ReportHabitCreateResponse.from(reportHabitRepository.save(habit));
    }

    @Transactional
    public ReportHabitCreateResponse update(Long id, ReportHabitCreateRequest request) {
        ReportHabit habit = getOrThrow(id);
        habit.update(
                request.habitName(),
                request.habitDivision(),
                request.habitPoint()
        );

        return ReportHabitCreateResponse.from(habit);
    }

    private ReportHabit getOrThrow(Long id) {
        return reportHabitRepository.findByIdAndOwner(id, SecurityUtil.getRequiredCurrentUserId())
                .orElseThrow(() -> new EntityNotFoundException("ReportHabit not found: " + id));
    }
}
