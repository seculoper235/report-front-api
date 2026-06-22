package com.example.reportfrontapi.domain.point.policy;

/**
 * 오목(로그) 차감 곡선: {@code y = a·ln(x) + b}, 결과를 [min, max]로 clip.
 * 배달러 금액 차감에 사용. 적은 금액(최소 주문가)에서 기울기가 가장 크다.
 */
public final class LogDeductionCurve implements DeductionCurve {
    private final double a;
    private final double b;
    private final int min;
    private final int max;

    public LogDeductionCurve(double a, double b, int min, int max) {
        this.a = a;
        this.b = b;
        this.min = min;
        this.max = max;
    }

    @Override
    public int pointsFor(long amount) {
        if (amount <= 0) {
            return min;
        }
        double y = a * Math.log(amount) + b;
        long rounded = Math.round(y);
        return (int) Math.max(min, Math.min(max, rounded));
    }
}
