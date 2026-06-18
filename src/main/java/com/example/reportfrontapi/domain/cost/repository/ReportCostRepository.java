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
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ReportCostRepository extends BaseRepository<ReportCost, Long> {

    private static final QReportCost cost = QReportCost.reportCost;

    public ReportCostRepository(EntityManager em) {
        super(ReportCost.class, em);
    }

    // paymentAt이 [start, end) 범위에 드는 소비 조회.
    public List<ReportCost> findByPaymentAtRange(LocalDateTime start, LocalDateTime end) {
        return selectFrom(cost)
                .where(
                        cost.paymentAt.goe(start),
                        cost.paymentAt.lt(end))
                .fetch();
    }

    // 카테고리 이름이 일치하는 소비 조회.
    public List<ReportCost> findByCategoryName(String categoryName) {
        return selectFrom(cost)
                .where(cost.categoryName.eq(categoryName))
                .fetch();
    }

    // 소비유형(division)/기간(start~end)을 선택적으로 적용해 페이징 조회. null인 조건은 무시.
    // 정렬은 Pageable의 sort로 처리(paymentAt / costPoint / costAmount).
    public Page<ReportCost> search(CostDivision division, LocalDateTime start, LocalDateTime end, Pageable pageable) {
        List<ReportCost> content = selectFrom(cost)
                .where(
                        divisionEq(division),
                        paymentAtGoe(start),
                        paymentAtLt(end))
                .orderBy(toOrderSpecifiers(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = select(cost.count())
                .from(cost)
                .where(
                        divisionEq(division),
                        paymentAtGoe(start),
                        paymentAtLt(end));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    // 전체 순포인트 합계: GOOD은 +costPoint, BAD는 -costPoint (division/point가 null인 건 제외).
    public int sumNetPoint() {
        NumberExpression<Integer> netPoint = new CaseBuilder()
                .when(cost.costDivision.eq(CostDivision.GOOD)).then(cost.costPoint)
                .otherwise(cost.costPoint.negate());

        Integer sum = select(netPoint.sumAggregate())
                .from(cost)
                .where(
                        cost.costDivision.isNotNull(),
                        cost.costPoint.isNotNull())
                .fetchOne();

        return sum != null ? sum : 0;
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
