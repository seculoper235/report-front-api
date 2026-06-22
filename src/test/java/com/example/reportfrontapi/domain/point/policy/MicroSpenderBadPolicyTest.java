package com.example.reportfrontapi.domain.point.policy;

import com.example.reportfrontapi.domain.user.model.CostPersona;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MicroSpenderBadPolicyTest {
    private final BadSpendingPolicy policy =
            new MicroSpenderBadPolicy(new PowerDeductionCurve(25, 15000, 1.3));

    @Test
    void 페르소나는_소과금러다() {
        assertThat(policy.persona()).isEqualTo(CostPersona.MICRO_SPENDER);
    }

    @Test
    void 금액과_당일개수를_합산한다() {
        // 15000원 → 금액 25, 개수 계단별 합산
        assertThat(policy.deduct(ctx(15000, 1))).isEqualTo(25);  // 25 + 0
        assertThat(policy.deduct(ctx(15000, 2))).isEqualTo(30);  // 25 + 5
        assertThat(policy.deduct(ctx(15000, 3))).isEqualTo(33);  // 25 + 8
        assertThat(policy.deduct(ctx(15000, 4))).isEqualTo(37);  // 25 + 12
        assertThat(policy.deduct(ctx(15000, 5))).isEqualTo(40);  // 25 + 15
        assertThat(policy.deduct(ctx(15000, 6))).isEqualTo(45);  // 25 + 20
        assertThat(policy.deduct(ctx(15000, 7))).isEqualTo(45);  // 6개 이상 → 20 유지
    }

    @Test
    void 합산_최소_최대_범위() {
        assertThat(policy.deduct(ctx(0, 0))).isEqualTo(0);     // 최소
        assertThat(policy.deduct(ctx(15000, 6))).isEqualTo(45); // 최대 25 + 20
    }

    private BadSpendingContext ctx(long amount, int dailyBadCount) {
        return new BadSpendingContext(amount, dailyBadCount, 0.0);
    }
}
