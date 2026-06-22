package com.example.reportfrontapi.domain.cost.repository;

import com.example.reportfrontapi.common.dto.Yn;
import com.example.reportfrontapi.common.repository.BaseRepository;
import com.example.reportfrontapi.domain.category.model.QCostCategory;
import com.example.reportfrontapi.domain.cost.model.CostDivision;
import com.example.reportfrontapi.domain.cost.model.QReportCost;
import com.example.reportfrontapi.domain.cost.model.ReportCost;
import com.example.reportfrontapi.domain.cost.controller.dto.ReportCostFindResponse;
import com.querydsl.core.types.Expression;
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
    private static final QCostCategory category = QCostCategory.costCategory;

    public ReportCostRepository(EntityManager em) {
        super(ReportCost.class, em);
    }

    // 소유자(crt_by) + id로 단건 조회. 타 사용자 행은 조회되지 않는다. (수정/삭제용 엔티티 반환)
    public Optional<ReportCost> findByIdAndOwner(Long id, Long userId) {
        return Optional.ofNullable(
                selectFrom(cost)
                        .where(cost.reportCostId.eq(id), ownerEq(userId))
                        .fetchOne());
    }

    // 소유자 + id 단건을 category innerJoin 으로 함께 조회해 응답 DTO로 반환.
    public Optional<ReportCostFindResponse> findResponseByIdAndOwner(Long id, Long userId) {
        return Optional.ofNullable(
                select(ReportCostFindResponse.class, reportCostProjection())
                        .from(cost)
                        .innerJoin(cost.category, category)
                        .where(cost.reportCostId.eq(id), ownerEq(userId))
                        .fetchOne());
    }

    // fixedYn=Y(고정 지출)이면서 지출 일시(paymentAt)의 '일(day)'이 dayOfMonth와 일치하는 소비 조회.
    // 매일 자정 스케줄러가 당일 일자에 해당하는 고정 지출을 찾아 당월 소비로 등록하는 데 사용한다.
    // 단, 당일이 그 달의 말일(lastDayOfMonth=true)이면 당월에 존재하지 않는 일자(예: 30/31일)의
    // 고정 지출도 함께 말일로 보정해 등록한다.
    // category를 innerJoin fetch로 함께 로딩한다.
    public List<ReportCost> findFixedCostsForDay(int dayOfMonth, boolean lastDayOfMonth) {
        BooleanExpression dayMatches = lastDayOfMonth
                ? cost.paymentAt.dayOfMonth().goe(dayOfMonth)
                : cost.paymentAt.dayOfMonth().eq(dayOfMonth);

        return selectFrom(cost)
                .innerJoin(cost.category, category).fetchJoin()
                .where(
                        cost.fixedYn.eq(Yn.Y),
                        dayMatches)
                .fetch();
    }

    // paymentAt이 [start, end) 범위에 드는 소유자 소비 조회. category를 innerJoin fetch로 함께 로딩.
    public List<ReportCost> findByPaymentAtRange(LocalDateTime start, LocalDateTime end, Long userId) {
        return selectFrom(cost)
                .innerJoin(cost.category, category).fetchJoin()
                .where(
                        ownerEq(userId),
                        cost.paymentAt.goe(start),
                        cost.paymentAt.lt(end))
                .fetch();
    }

    // 카테고리(RPT_COST_CAT) ID가 일치하는 소유자 소비를 category innerJoin 으로 조회해 응답 DTO로 반환.
    public List<ReportCostFindResponse> findByCategoryId(Long categoryId, Long userId) {
        return select(ReportCostFindResponse.class, reportCostProjection())
                .from(cost)
                .innerJoin(cost.category, category)
                .where(ownerEq(userId), category.categoryId.eq(categoryId))
                .fetch();
    }

    // 소비유형(division)/기간(start~end)을 선택적으로 적용해 소유자 기준 페이징 조회. null인 조건은 무시.
    // 정렬은 Pageable의 sort로 처리(paymentAt / costPoint / costAmount).
    // 페이지 조립(총건수 결합)은 Service에서 처리하므로 여기서는 현재 페이지 데이터만 반환한다.
    // TODO: 추후 Service 단에서 페이징 처리 이관 고려 필요
    public List<ReportCostFindResponse> search(CostDivision division, LocalDateTime start, LocalDateTime end,
                                           Pageable pageable, Long userId) {
        return select(ReportCostFindResponse.class, reportCostProjection())
                .from(cost)
                .innerJoin(cost.category, category)
                .where(
                        hasCostDivision(),
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
                        hasCostDivision(),
                        ownerEq(userId),
                        divisionEq(division),
                        paymentAtGoe(start),
                        paymentAtLt(end))
                .fetchOne();

        return count != null ? count : 0L;
    }

    // 해당 카테고리를 참조하는 소유자 소비가 하나라도 있는지 여부(카테고리 삭제 가드용).
    public boolean existsByCategoryId(Long categoryId, Long userId) {
        return selectFrom(cost)
                .where(ownerEq(userId), cost.category.categoryId.eq(categoryId))
                .fetchFirst() != null;
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
                        hasCostDivision(),
                        ownerEq(userId),
                        cost.costDivision.isNotNull(),
                        cost.costPoint.isNotNull())
                .fetchOne();
    }

    // ReportCostFindResponse 생성자 순서에 맞춘 프로젝션. categoryId/categoryName은 innerJoin한 category에서 가져온다.
    private Expression<?>[] reportCostProjection() {
        return new Expression<?>[]{
                cost.reportCostId,
                category.categoryId,
                category.categoryName,
                cost.costName,
                cost.fixedYn,
                cost.costDescription,
                cost.amountDivision,
                cost.costAmount,
                cost.paymentMethod,
                cost.paymentAt,
                cost.costDivision,
                costPointOrZero()
        };
    }

    // 소비유형/포인트가 없으면 0. 엔티티 getCostPoint()와 동일한 정규화.
    private NumberExpression<Integer> costPointOrZero() {
        return new CaseBuilder()
                .when(cost.costDivision.isNull().or(cost.costPoint.isNull())).then(0)
                .otherwise(cost.costPoint);
    }

    private BooleanExpression hasCostDivision() {
        return cost.costDivision.isNotNull();
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
