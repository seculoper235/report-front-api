package com.example.reportfrontapi.domain.point.policy;

import com.example.reportfrontapi.domain.user.model.CostPersona;

/**
 * 배달러 차감 = 금액 곡선 + 최근 3일 배달 빈도 계단.
 * 빈도(1일 평균): {@code <0.3 → 0}, {@code <0.7 → 5}, {@code ≥0.7 → 15}(최대).
 */
public class DeliveryBadPolicy implements BadSpendingPolicy {
    private final DeductionCurve amountCurve;

    public DeliveryBadPolicy(DeductionCurve amountCurve) {
        this.amountCurve = amountCurve;
    }

    @Override
    public CostPersona persona() {
        return CostPersona.DELIVERY;
    }

    @Override
    public int deduct(BadSpendingContext ctx) {
        return amountCurve.pointsFor(ctx.amount()) + frequencyPoint(ctx.deliveryDailyAverage());
    }

    private int frequencyPoint(double dailyAverage) {
        if (dailyAverage < 0.3) {
            return 0;
        }
        if (dailyAverage < 0.7) {
            return 5;
        }
        return 15; // 0.7 이상은 모두 최대값 15
    }
}
