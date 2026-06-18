package com.example.reportfrontapi.domain.habit.repository;

import com.example.reportfrontapi.domain.habit.ReportHabit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportHabitRepository extends JpaRepository<ReportHabit, Long> {
}
