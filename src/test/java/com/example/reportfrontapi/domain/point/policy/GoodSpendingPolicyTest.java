package com.example.reportfrontapi.domain.point.policy;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GoodSpendingPolicyTest {
    // 랜덤은 항상 7을 주는 결정적 구현으로 고정
    private final GoodSpendingPolicy policy =
            new GoodSpendingPolicy(1, 10, 50, (min, max) -> 7);

    @Test
    void 지정값은_min_max로_clamp한다() {
        assertThat(policy.award(5, 0)).isEqualTo(5);
        assertThat(policy.award(20, 0)).isEqualTo(10); // max 초과
        assertThat(policy.award(0, 0)).isEqualTo(1);   // min 미만
    }

    @Test
    void 미지정시_랜덤값을_쓴다() {
        assertThat(policy.award(null, 0)).isEqualTo(7);
    }

    @Test
    void 일_한도_50을_넘지_않게_자른다() {
        assertThat(policy.award(8, 45)).isEqualTo(5);   // 잔여 5
        assertThat(policy.award(8, 50)).isEqualTo(0);   // 잔여 0
        assertThat(policy.award(null, 48)).isEqualTo(2); // 랜덤 7이지만 잔여 2
    }

    @Test
    void 랜덤은_min_max_범위를_요청한다() {
        int[] captured = new int[2];
        GoodSpendingPolicy capturing = new GoodSpendingPolicy(1, 10, 50, (min, max) -> {
            captured[0] = min;
            captured[1] = max;
            return min;
        });
        capturing.award(null, 0);
        assertThat(captured).containsExactly(1, 10);
    }
}
