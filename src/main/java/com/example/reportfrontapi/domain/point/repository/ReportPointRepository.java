package com.example.reportfrontapi.domain.point.repository;

import com.example.reportfrontapi.common.repository.BaseRepository;
import com.example.reportfrontapi.domain.point.QReportPoint;
import com.example.reportfrontapi.domain.point.ReportPoint;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ReportPointRepository extends BaseRepository<ReportPoint, Long> {

    private static final QReportPoint point = QReportPoint.reportPoint;

    public ReportPointRepository(EntityManager em) {
        super(ReportPoint.class, em);
    }

    // 사용자 잔액 = delta 합계. 항목이 없으면 null.
    public Integer sumByUserId(Long userId) {
        return select(point.delta.sumAggregate())
                .from(point)
                .where(point.userId.eq(userId))
                .fetchOne();
    }

    // 사용자 원장 내역(최신순).
    public List<ReportPoint> findByUserId(Long userId) {
        return selectFrom(point)
                .where(point.userId.eq(userId))
                .orderBy(point.createdAt.desc(), point.reportPointId.desc())
                .fetch();
    }
}
