package com.example.reportfrontapi.domain.point.repository;

import com.example.reportfrontapi.common.repository.BaseRepository;
import com.example.reportfrontapi.domain.point.model.PointAmountDivision;
import com.example.reportfrontapi.domain.point.model.PointReason;
import com.example.reportfrontapi.domain.point.model.QReportPoint;
import com.example.reportfrontapi.domain.point.model.ReportPoint;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ReportPointRepository extends BaseRepository<ReportPoint, Long> {

    private static final QReportPoint point = QReportPoint.reportPoint;

    public ReportPointRepository(EntityManager em) {
        super(ReportPoint.class, em);
    }

    // 사용자 잔액 = 적립(+point_amt) − 차감(-point_amt) 합계. 항목이 없으면 null.
    public Integer sumByUserId(Long userId) {
        NumberExpression<Integer> signed = new CaseBuilder()
                .when(point.pointAmountDivision.eq(PointAmountDivision.INCREASE)).then(point.pointAmount)
                .otherwise(point.pointAmount.negate());

        return select(signed.sumAggregate())
                .from(point)
                .where(point.userId.eq(userId))
                .fetchOne();
    }

    // 해당 사유의 원장 항목이 [start, end) 안에 이미 있는지(매일 보너스 중복 지급 방지용).
    public boolean existsByUserReasonAndCreatedAtRange(Long userId, PointReason reason,
                                                       LocalDateTime start, LocalDateTime end) {
        return select(point.reportPointId)
                .from(point)
                .where(
                        point.userId.eq(userId),
                        point.reason.eq(reason),
                        point.createdAt.goe(start),
                        point.createdAt.lt(end))
                .fetchFirst() != null;
    }

    // 사용자 원장 내역(최신순).
    public List<ReportPoint> findByUserId(Long userId) {
        return selectFrom(point)
                .where(point.userId.eq(userId))
                .orderBy(point.createdAt.desc(), point.reportPointId.desc())
                .fetch();
    }
}
