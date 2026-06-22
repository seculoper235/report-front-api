package com.example.reportfrontapi.domain.point.policy;

/**
 * 볼록(거듭제곱) 차감 곡선: {@code y = maxPoint · (x/xMax)^exponent}, xMax 초과는 maxPoint로 cap.
 * 소과금러 금액 차감에 사용. 단건 금액이 클수록 차감이 가속한다.
 */
public final class PowerDeductionCurve implements DeductionCurve {
    private final double maxPoint;
    private final double xMax;
    private final double exponent;

    public PowerDeductionCurve(double maxPoint, double xMax, double exponent) {
        this.maxPoint = maxPoint;
        this.xMax = xMax;
        this.exponent = exponent;
    }

    @Override
    public int pointsFor(long amount) {
        if (amount <= 0) {
            return 0;
        }
        double ratio = Math.min(1.0, amount / xMax);
        double y = maxPoint * Math.pow(ratio, exponent);
        return (int) Math.round(y);
    }
}
