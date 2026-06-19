package com.example.reportfrontapi.domain.point.repository;

import com.example.reportfrontapi.common.repository.BaseRepository;
import com.example.reportfrontapi.domain.point.PointAmountDivision;
import com.example.reportfrontapi.domain.point.QReportPoint;
import com.example.reportfrontapi.domain.point.ReportPoint;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

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

    // 사용자 원장 내역(최신순).
    public List<ReportPoint> findByUserId(Long userId) {
        return selectFrom(point)
                .where(point.userId.eq(userId))
                .orderBy(point.createdAt.desc(), point.reportPointId.desc())
                .fetch();
    }
}
