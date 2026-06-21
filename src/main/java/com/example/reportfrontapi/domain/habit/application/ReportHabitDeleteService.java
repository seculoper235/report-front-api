package com.example.reportfrontapi.domain.habit.application;

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
public class ReportHabitDeleteService {
    private final ReportHabitRepository reportHabitRepository;

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
