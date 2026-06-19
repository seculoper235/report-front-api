package com.example.reportfrontapi.domain.habit.repository;

import com.example.reportfrontapi.common.repository.BaseRepository;
import com.example.reportfrontapi.domain.habit.QReportHabit;
import com.example.reportfrontapi.domain.habit.ReportHabit;
import com.querydsl.core.types.dsl.BooleanExpression;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ReportHabitRepository extends BaseRepository<ReportHabit, Long> {

    private static final QReportHabit habit = QReportHabit.reportHabit;

    public ReportHabitRepository(EntityManager em) {
        super(ReportHabit.class, em);
    }

    // 소유자(crt_by) + id로 단건 조회. 타 사용자 행은 조회되지 않는다.
    public Optional<ReportHabit> findByIdAndOwner(Long id, Long userId) {
        return Optional.ofNullable(
                selectFrom(habit)
                        .where(habit.reportCostId.eq(id), ownerEq(userId))
                        .fetchOne());
    }

    // 소유자의 전체 습관 조회.
    public List<ReportHabit> findAllByOwner(Long userId) {
        return selectFrom(habit)
                .where(ownerEq(userId))
                .fetch();
    }

    // createdAt(crt_at)이 [start, end) 범위에 드는 소유자 습관 조회. createdAt은 BaseEntity 상속 필드.
    public List<ReportHabit> findByCreatedAtRange(LocalDateTime start, LocalDateTime end, Long userId) {
        return selectFrom(habit)
                .where(
                        ownerEq(userId),
                        habit.createdAt.goe(start),
                        habit.createdAt.lt(end))
                .fetch();
    }

    // 소유자(user_id) 일치 조건.
    private BooleanExpression ownerEq(Long userId) {
        return habit.userId.eq(userId);
    }
}
