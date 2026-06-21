package com.example.reportfrontapi.common.repository;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaQuery;

import java.util.List;
import java.util.Optional;

/**
 * QueryDSL 기반 공통 베이스 리포지토리.
 * JpaRepository를 상속하지 않고 EntityManager로 기본 CRUD를,
 * JPAQueryFactory로 동적 쿼리를 직접 처리한다.
 * 하위 구상 클래스에서는 queryFactory. prefix 없이 select(...)/selectFrom(...)을 바로 호출할 수 있다.
 *
 * @param <T>  엔티티 타입
 * @param <ID> 식별자 타입
 */
public abstract class BaseRepository<T, ID> {

    private final Class<T> entityClass;
    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    protected BaseRepository(Class<T> entityClass, EntityManager em) {
        this.entityClass = entityClass;
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    // ===== 기본 CRUD =====

    public T save(T entity) {
        Object id = em.getEntityManagerFactory()
                .getPersistenceUnitUtil()
                .getIdentifier(entity);
        if (id == null) {
            em.persist(entity);
            return entity;
        }
        return em.merge(entity);
    }

    public Optional<T> findById(ID id) {
        return Optional.ofNullable(em.find(entityClass, id));
    }

    public List<T> findAll() {
        CriteriaQuery<T> query = em.getCriteriaBuilder().createQuery(entityClass);
        query.select(query.from(entityClass));
        return em.createQuery(query).getResultList();
    }

    public void delete(T entity) {
        em.remove(em.contains(entity) ? entity : em.merge(entity));
    }

    // ===== QueryDSL 위임 =====

    protected <R> JPAQuery<R> select(Expression<R> expr) {
        return queryFactory.select(expr);
    }

    /**
     * DTO 생성자 프로젝션. 전달한 표현식 순서대로 type의 생성자(레코드 포함)에 매핑된다.
     * 예) select(ReportCostResponse.class, cost.reportCostId, category.categoryId, ...)
     *         .from(cost).innerJoin(cost.category, category).fetch();
     */
    protected <R> JPAQuery<R> select(Class<R> type, Expression<?>... exprs) {
        return queryFactory.select(Projections.constructor(type, exprs));
    }

    protected <R> JPAQuery<R> selectFrom(EntityPath<R> from) {
        return queryFactory.selectFrom(from);
    }

    protected JPAUpdateClause update(EntityPath<? extends T> path) {
        return queryFactory.update(path);
    }
}
