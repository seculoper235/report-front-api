package com.example.reportfrontapi.domain.habit.repository;

import com.example.reportfrontapi.domain.habit.ReportHabit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportHabitRepository extends JpaRepository<ReportHabit, Long> {

    // createdAt(crt_at)이 [start, end) 범위에 드는 습관 조회. createdAt은 BaseEntity 상속 필드.
    List<ReportHabit> findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime start, LocalDateTime end);
}
