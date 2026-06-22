package com.example.reportfrontapi.domain.point.policy;

import com.example.reportfrontapi.domain.user.model.CostPersona;

/**
 * 소과금러 차감 = 금액 곡선 + 당일 BAD 개수 계단.
 * 개수: {@code 2→5, 3→8, 4→12, 5→15, 6개 이상→20}.
 */
public class MicroSpenderBadPolicy implements BadSpendingPolicy {
    private final DeductionCurve amountCurve;

    public MicroSpenderBadPolicy(DeductionCurve amountCurve) {
        this.amountCurve = amountCurve;
    }

    @Override
    public CostPersona persona() {
        return CostPersona.MICRO_SPENDER;
    }

    @Override
    public int deduct(BadSpendingContext ctx) {
        return amountCurve.pointsFor(ctx.amount()) + countPoint(ctx.dailyBadCount());
    }

    private int countPoint(int dailyBadCount) {
        if (dailyBadCount <= 1) {
            return 0;
        }
        return switch (dailyBadCount) {
            case 2 -> 5;
            case 3 -> 8;
            case 4 -> 12;
            case 5 -> 15;
            default -> 20; // 6개 이상
        };
    }
}
