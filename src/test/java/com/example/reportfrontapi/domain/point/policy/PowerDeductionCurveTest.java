package com.example.reportfrontapi.domain.point.policy;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PowerDeductionCurveTest {
    // 소과금러 금액 곡선: y = 25·(x/15000)^1.3, 25 cap
    private final DeductionCurve curve = new PowerDeductionCurve(25, 15000, 1.3);

    @Test
    void 좌표값을_곡선으로_재현한다() {
        assertThat(curve.pointsFor(0)).isEqualTo(0);
        assertThat(curve.pointsFor(4000)).isEqualTo(4);
        assertThat(curve.pointsFor(8000)).isEqualTo(11);
        assertThat(curve.pointsFor(10000)).isEqualTo(15);
        assertThat(curve.pointsFor(15000)).isEqualTo(25);
    }

    @Test
    void xMax를_넘으면_상한으로_cap한다() {
        assertThat(curve.pointsFor(20000)).isEqualTo(25);
        assertThat(curve.pointsFor(100000)).isEqualTo(25);
    }
}
