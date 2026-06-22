package com.example.reportfrontapi.domain.point.policy;

import com.example.reportfrontapi.domain.user.model.CostPersona;

/**
 * 페르소나별 나쁜 소비(BAD) 차감 정책. 금액 성분 + 빈도/개수 성분을 합산한다.
 */
public interface BadSpendingPolicy {
    /** 이 정책이 담당하는 페르소나. */
    CostPersona persona();

    /** 차감 포인트(양수 크기). */
    int deduct(BadSpendingContext ctx);
}
