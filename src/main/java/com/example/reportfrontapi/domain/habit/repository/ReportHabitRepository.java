package com.example.reportfrontapi.domain.habit.repository;

import com.example.reportfrontapi.common.repository.BaseRepository;
import com.example.reportfrontapi.domain.habit.QReportHabit;
import com.example.reportfrontapi.domain.habit.ReportHabit;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ReportHabitRepository extends BaseRepository<ReportHabit, Long> {

    private static final QReportHabit habit = QReportHabit.reportHabit;

    public ReportHabitRepository(EntityManager em) {
        super(ReportHabit.class, em);
    }

    // createdAt(crt_at)이 [start, end) 범위에 드는 습관 조회. createdAt은 BaseEntity 상속 필드.
    public List<ReportHabit> findByCreatedAtRange(LocalDateTime start, LocalDateTime end) {
        return selectFrom(habit)
                .where(
                        habit.createdAt.goe(start),
                        habit.createdAt.lt(end))
                .fetch();
    }
}
