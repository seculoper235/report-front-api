package com.example.reportfrontapi.domain.point.policy;

/**
 * 금액 → 차감 포인트 곡선. 페르소나 정책에서 금액 성분 계산에 쓰인다.
 * 구현체를 갈아끼우면(또는 계수만 바꾸면) 차감 곡선이 통째로 교체된다.
 */
@FunctionalInterface
public interface DeductionCurve {
    /** 금액(원)에 대한 차감 포인트(양수 크기). */
    int pointsFor(long amount);
}
