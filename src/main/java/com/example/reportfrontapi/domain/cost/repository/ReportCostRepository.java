package com.example.reportfrontapi.domain.cost.repository;

import com.example.reportfrontapi.common.repository.BaseRepository;
import com.example.reportfrontapi.domain.cost.CostDivision;
import com.example.reportfrontapi.domain.cost.QReportCost;
import com.example.reportfrontapi.domain.cost.ReportCost;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ReportCostRepository extends BaseRepository<ReportCost, Long> {

    private static final QReportCost cost = QReportCost.reportCost;

    public ReportCostRepository(EntityManager em) {
        super(ReportCost.class, em);
    }

    // 소유자(crt_by) + id로 단건 조회. 타 사용자 행은 조회되지 않는다.
    public Optional<ReportCost> findByIdAndOwner(Long id, Long userId) {
        return Optional.ofNullable(
                selectFrom(cost)
                        .where(cost.reportCostId.eq(id), ownerEq(userId))
                        .fetchOne());
    }

    // paymentAt이 [start, end) 범위에 드는 소유자 소비 조회.
    public List<ReportCost> findByPaymentAtRange(LocalDateTime start, LocalDateTime end, Long userId) {
        return selectFrom(cost)
                .where(
                        ownerEq(userId),
                        cost.paymentAt.goe(start),
                        cost.paymentAt.lt(end))
                .fetch();
    }

    // 카테고리 이름이 일치하는 소유자 소비 조회.
    public List<ReportCost> findByCategoryName(String categoryName, Long userId) {
        return selectFrom(cost)
                .where(ownerEq(userId), cost.categoryName.eq(categoryName))
                .fetch();
    }

    // 소비유형(division)/기간(start~end)을 선택적으로 적용해 소유자 기준 페이징 조회. null인 조건은 무시.
    // 정렬은 Pageable의 sort로 처리(paymentAt / costPoint / costAmount).
    // 페이지 조립(총건수 결합)은 Service에서 처리하므로 여기서는 현재 페이지 데이터만 반환한다.
    // TODO: 추후 Service 단에서 페이징 처리 이관 고려 필요
    public List<ReportCost> search(CostDivision division, LocalDateTime start, LocalDateTime end,
                                   Pageable pageable, Long userId) {
        return selectFrom(cost)
                .where(
                        ownerEq(userId),
                        divisionEq(division),
                        paymentAtGoe(start),
                        paymentAtLt(end))
                .orderBy(toOrderSpecifiers(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    // search와 동일한 조건의 전체 건수(페이징 totalElements 산출용).
    public long countSearch(CostDivision division, LocalDateTime start, LocalDateTime end, Long userId) {
        Long count = select(cost.count())
                .from(cost)
                .where(
                        ownerEq(userId),
                        divisionEq(division),
                        paymentAtGoe(start),
                        paymentAtLt(end))
                .fetchOne();

        return count != null ? count : 0L;
    }

    // 소유자 전체 순포인트 합계 집계: GOOD은 +costPoint, BAD는 -costPoint (division/point가 null인 건 제외).
    // 집계 대상이 없으면 null을 반환하며, 기본값 처리는 Service에서 한다.
    public Integer sumNetPoint(Long userId) {
        NumberExpression<Integer> netPoint = new CaseBuilder()
                .when(cost.costDivision.eq(CostDivision.GOOD)).then(cost.costPoint)
                .otherwise(cost.costPoint.negate());

        return select(netPoint.sumAggregate())
                .from(cost)
                .where(
                        ownerEq(userId),
                        cost.costDivision.isNotNull(),
                        cost.costPoint.isNotNull())
                .fetchOne();
    }

    // 소유자(user_id) 일치 조건.
    private BooleanExpression ownerEq(Long userId) {
        return cost.userId.eq(userId);
    }

    private BooleanExpression divisionEq(CostDivision division) {
        return division != null ? cost.costDivision.eq(division) : null;
    }

    private BooleanExpression paymentAtGoe(LocalDateTime start) {
        return start != null ? cost.paymentAt.goe(start) : null;
    }

    private BooleanExpression paymentAtLt(LocalDateTime end) {
        return end != null ? cost.paymentAt.lt(end) : null;
    }

    // Pageable의 Sort를 QueryDSL OrderSpecifier로 변환. 정렬 가능 필드: paymentAt / costPoint / costAmount
    private OrderSpecifier<?>[] toOrderSpecifiers(Sort sort) {
        PathBuilder<ReportCost> path = new PathBuilder<>(ReportCost.class, cost.getMetadata());

        List<OrderSpecifier<?>> orders = new ArrayList<>();
        for (Sort.Order order : sort) {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            orders.add(new OrderSpecifier<>(direction, path.getComparable(order.getProperty(), Comparable.class)));
        }
        return orders.toArray(new OrderSpecifier[0]);
    }
}
