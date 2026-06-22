package com.example.reportfrontapi.domain.point.policy;

import com.example.reportfrontapi.domain.user.model.CostPersona;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeliveryBadPolicyTest {
    private final BadSpendingPolicy policy =
            new DeliveryBadPolicy(new LogDeductionCurve(13.32, -105.5, 5, 35));

    @Test
    void 페르소나는_배달러다() {
        assertThat(policy.persona()).isEqualTo(CostPersona.DELIVERY);
    }

    @Test
    void 금액과_빈도를_합산한다() {
        // 8000원 → 금액 14, 빈도 계단별 합산
        assertThat(policy.deduct(ctx(8000, 0.2))).isEqualTo(14);  // 14 + 0
        assertThat(policy.deduct(ctx(8000, 0.5))).isEqualTo(19);  // 14 + 5
        assertThat(policy.deduct(ctx(8000, 0.8))).isEqualTo(29);  // 14 + 15
        assertThat(policy.deduct(ctx(8000, 1.5))).isEqualTo(29);  // 14 + 15(최대)
    }

    @Test
    void 빈도_계단_경계() {
        assertThat(policy.deduct(ctx(4000, 0.29))).isEqualTo(5);   // 5 + 0
        assertThat(policy.deduct(ctx(4000, 0.30))).isEqualTo(10);  // 5 + 5
        assertThat(policy.deduct(ctx(4000, 0.69))).isEqualTo(10);  // 5 + 5
        assertThat(policy.deduct(ctx(4000, 0.70))).isEqualTo(20);  // 5 + 15
    }

    @Test
    void 합산_최소_최대_범위() {
        assertThat(policy.deduct(ctx(4000, 0.0))).isEqualTo(5);    // 최소
        assertThat(policy.deduct(ctx(38000, 1.0))).isEqualTo(50);  // 최대 35 + 15
    }

    private BadSpendingContext ctx(long amount, double deliveryDailyAverage) {
        return new BadSpendingContext(amount, 1, deliveryDailyAverage);
    }
}
