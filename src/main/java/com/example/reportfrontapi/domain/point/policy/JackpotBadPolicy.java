package com.example.reportfrontapi.domain.point.policy;

import com.example.reportfrontapi.domain.user.model.CostPersona;

/**
 * 한탕주의 차감 정책 — 미구현 빈 스텁.
 * TODO: 금액이 높을수록 차감율이 커지는 곡선 구현 예정. 현재는 0을 반환한다.
 */
public class JackpotBadPolicy implements BadSpendingPolicy {

    @Override
    public CostPersona persona() {
        return CostPersona.JACKPOT;
    }

    @Override
    public int deduct(BadSpendingContext ctx) {
        return 0;
    }
}
