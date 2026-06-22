package com.example.reportfrontapi.domain.cost.repository;

import com.example.reportfrontapi.common.repository.BaseRepository;
import com.example.reportfrontapi.domain.cost.model.CostDivision;
import com.example.reportfrontapi.domain.cost.model.CostPoint;
import com.example.reportfrontapi.domain.cost.model.QCostPoint;
import com.example.reportfrontapi.domain.cost.model.QReportCost;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class CostPointRepository extends BaseRepository<CostPoint, Long> {

    private static final QCostPoint costPoint = QCostPoint.costPoint;
    private static final QReportCost cost = QReportCost.reportCost;

    public CostPointRepository(EntityManager em) {
        super(CostPoint.class, em);
    }

    // 소비 내역 ID로 포인트 단건 조회(수정 시 재계산용).
    public Optional<CostPoint> findByReportCostId(Long reportCostId, Long userId) {
        return Optional.ofNullable(
                selectFrom(costPoint)
                        .where(costPoint.reportCostId.eq(reportCostId), costPoint.userId.eq(userId))
                        .fetchOne());
    }

    // 소유자 전체 순포인트 합계(GOOD +, BAD −). 집계 대상이 없으면 null.
    public Integer sumNetPoint(Long userId) {
        NumberExpression<Integer> netPoint = new CaseBuilder()
                .when(costPoint.division.eq(CostDivision.GOOD)).then(costPoint.pointAmount)
                .otherwise(costPoint.pointAmount.negate());

        return select(netPoint.sumAggregate())
                .from(costPoint)
                .where(costPoint.userId.eq(userId))
                .fetchOne();
    }

    // 당일(소비 paymentAt 기준) 누적 GOOD 포인트 합계. 좋은 소비 일 한도(50) 검사용.
    public int sumGoodPointByPaymentDay(Long userId, LocalDateTime dayStart, LocalDateTime dayEnd) {
        Integer sum = select(costPoint.pointAmount.sumAggregate())
                .from(costPoint, cost)
                .where(
                        costPoint.reportCostId.eq(cost.reportCostId),
                        costPoint.userId.eq(userId),
                        costPoint.division.eq(CostDivision.GOOD),
                        cost.paymentAt.goe(dayStart),
                        cost.paymentAt.lt(dayEnd))
                .fetchOne();
        return sum != null ? sum : 0;
    }
}
