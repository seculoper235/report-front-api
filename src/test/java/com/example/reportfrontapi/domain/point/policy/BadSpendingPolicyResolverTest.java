package com.example.reportfrontapi.domain.point.policy;

import com.example.reportfrontapi.domain.user.model.CostPersona;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BadSpendingPolicyResolverTest {
    private final BadSpendingPolicyResolver resolver = new BadSpendingPolicyResolver(List.of(
            new DeliveryBadPolicy(new LogDeductionCurve(13.32, -105.5, 5, 35)),
            new MicroSpenderBadPolicy(new PowerDeductionCurve(25, 15000, 1.3)),
            new JackpotBadPolicy()
    ));

    @Test
    void 페르소나에_맞는_정책으로_라우팅한다() {
        // 배달러: 38000원 + 빈도1.0 → 35 + 15 = 50
        assertThat(resolver.deduct(CostPersona.DELIVERY, new BadSpendingContext(38000, 1, 1.0)))
                .isEqualTo(50);
        // 소과금러: 15000원 + 6개 → 25 + 20 = 45
        assertThat(resolver.deduct(CostPersona.MICRO_SPENDER, new BadSpendingContext(15000, 6, 0.0)))
                .isEqualTo(45);
    }

    @Test
    void 한탕주의는_미구현이라_0이다() {
        assertThat(resolver.deduct(CostPersona.JACKPOT, new BadSpendingContext(99999, 9, 9.0)))
                .isEqualTo(0);
    }

    @Test
    void 등록되지_않은_페르소나는_예외다() {
        BadSpendingPolicyResolver onlyJackpot =
                new BadSpendingPolicyResolver(List.of(new JackpotBadPolicy()));
        assertThatThrownBy(() -> onlyJackpot.deduct(CostPersona.DELIVERY, new BadSpendingContext(1000, 1, 0.0)))
                .isInstanceOf(IllegalStateException.class);
    }
}
