package com.example.reportfrontapi.domain.point.policy;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LogDeductionCurveTest {
    // 배달러 금액 곡선: y = 13.32·ln(x) − 105.5, [5, 35] clip
    private final DeductionCurve curve = new LogDeductionCurve(13.32, -105.5, 5, 35);

    @Test
    void 좌표값을_곡선으로_재현한다() {
        assertThat(curve.pointsFor(4000)).isEqualTo(5);
        assertThat(curve.pointsFor(8000)).isEqualTo(14);
        assertThat(curve.pointsFor(10000)).isEqualTo(17);
        assertThat(curve.pointsFor(15000)).isEqualTo(23);
        assertThat(curve.pointsFor(20000)).isEqualTo(26);
        assertThat(curve.pointsFor(38000)).isEqualTo(35);
    }

    @Test
    void 하한과_상한으로_clip한다() {
        assertThat(curve.pointsFor(0)).isEqualTo(5);     // 정의역 아래 → min
        assertThat(curve.pointsFor(1000)).isEqualTo(5);  // 음수 결과 → min
        assertThat(curve.pointsFor(50000)).isEqualTo(35); // 정의역 위 → max
    }
}
