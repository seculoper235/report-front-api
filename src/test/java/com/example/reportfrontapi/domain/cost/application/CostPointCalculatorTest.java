package com.example.reportfrontapi.domain.cost.application;

import com.example.reportfrontapi.domain.category.repository.CostCategoryRepository;
import com.example.reportfrontapi.domain.cost.model.CostDivision;
import com.example.reportfrontapi.domain.cost.repository.CostPointRepository;
import com.example.reportfrontapi.domain.cost.repository.ReportCostRepository;
import com.example.reportfrontapi.domain.point.policy.BadSpendingPolicyResolver;
import com.example.reportfrontapi.domain.point.policy.DeliveryBadPolicy;
import com.example.reportfrontapi.domain.point.policy.GoodSpendingPolicy;
import com.example.reportfrontapi.domain.point.policy.JackpotBadPolicy;
import com.example.reportfrontapi.domain.point.policy.LogDeductionCurve;
import com.example.reportfrontapi.domain.point.policy.MicroSpenderBadPolicy;
import com.example.reportfrontapi.domain.point.policy.PowerDeductionCurve;
import com.example.reportfrontapi.domain.user.model.CostPersona;
import com.example.reportfrontapi.domain.user.model.User;
import com.example.reportfrontapi.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CostPointCalculatorTest {
    private static final Long USER_ID = 1L;
    private static final LocalDateTime PAYMENT_AT = LocalDateTime.of(2026, 6, 22, 12, 0);

    @Mock ReportCostRepository reportCostRepository;
    @Mock CostPointRepository costPointRepository;
    @Mock CostCategoryRepository costCategoryRepository;
    @Mock UserRepository userRepository;

    // 정책은 실제 구현을 사용(곡선 계산까지 검증).
    private final GoodSpendingPolicy goodPolicy = new GoodSpendingPolicy(1, 10, 50, (min, max) -> 7);
    private final BadSpendingPolicyResolver badResolver = new BadSpendingPolicyResolver(List.of(
            new DeliveryBadPolicy(new LogDeductionCurve(13.32, -105.5, 5, 35)),
            new MicroSpenderBadPolicy(new PowerDeductionCurve(25, 15000, 1.3)),
            new JackpotBadPolicy()
    ));

    private CostPointCalculator calculator() {
        return new CostPointCalculator(goodPolicy, badResolver,
                reportCostRepository, costPointRepository, costCategoryRepository, userRepository);
    }

    @Test
    void 소비유형_미선택이면_0이다() {
        CostPointCalculator.Result result = calculator()
                .calculate(USER_ID, null, 10L, 8000, PAYMENT_AT, null, null);
        assertThat(result.point()).isZero();
        assertThat(result.persona()).isNull();
    }

    @Test
    void GOOD은_지정값을_clamp한다() {
        given(costPointRepository.sumGoodPointByPaymentDay(eq(USER_ID), any(), any())).willReturn(0);
        CostPointCalculator.Result result = calculator()
                .calculate(USER_ID, CostDivision.GOOD, 10L, 8000, PAYMENT_AT, 20, null);
        assertThat(result.point()).isEqualTo(10); // max clamp
        assertThat(result.persona()).isNull();
    }

    @Test
    void GOOD_미지정이면_랜덤이고_일한도로_자른다() {
        given(costPointRepository.sumGoodPointByPaymentDay(eq(USER_ID), any(), any())).willReturn(48);
        CostPointCalculator.Result result = calculator()
                .calculate(USER_ID, CostDivision.GOOD, 10L, 8000, PAYMENT_AT, null, null);
        assertThat(result.point()).isEqualTo(2); // 랜덤 7이지만 잔여 2
    }

    @Test
    void 페르소나_미선택이면_BAD도_0이다() {
        given(userRepository.findById(USER_ID)).willReturn(Optional.of(userWithPersona(null)));
        CostPointCalculator.Result result = calculator()
                .calculate(USER_ID, CostDivision.BAD, 10L, 8000, PAYMENT_AT, null, null);
        assertThat(result.point()).isZero();
    }

    @Test
    void 소과금러_BAD는_금액곡선과_당일개수를_합산한다() {
        given(userRepository.findById(USER_ID)).willReturn(Optional.of(userWithPersona(CostPersona.MICRO_SPENDER)));
        // 기존 당일 BAD 1건 → 이번 건 포함 2개 → 개수 성분 5, 금액 15000 → 25. 합 30.
        given(reportCostRepository.countBadByDay(eq(USER_ID), any(), any(), eq(null))).willReturn(1L);
        given(costCategoryRepository.findIdByNameAndOwner(any(), eq(USER_ID))).willReturn(Optional.empty());

        CostPointCalculator.Result result = calculator()
                .calculate(USER_ID, CostDivision.BAD, 10L, 15000, PAYMENT_AT, null, null);

        assertThat(result.point()).isEqualTo(30);
        assertThat(result.persona()).isEqualTo(CostPersona.MICRO_SPENDER);
    }

    @Test
    void 배달러_BAD는_금액곡선과_빈도를_합산한다() {
        given(userRepository.findById(USER_ID)).willReturn(Optional.of(userWithPersona(CostPersona.DELIVERY)));
        given(reportCostRepository.countBadByDay(eq(USER_ID), any(), any(), eq(null))).willReturn(0L);
        // 배달 카테고리 ID=99, 최근 3일 기존 2건 + 이번 건(배달 카테고리)=3 → 평균 1.0 → 빈도 15
        given(costCategoryRepository.findIdByNameAndOwner(any(), eq(USER_ID))).willReturn(Optional.of(99L));
        given(reportCostRepository.countBadByCategoryAndRange(eq(USER_ID), eq(99L), any(), any(), eq(null)))
                .willReturn(2L);

        // 금액 8000 → 14, 빈도 15 → 합 29
        CostPointCalculator.Result result = calculator()
                .calculate(USER_ID, CostDivision.BAD, 99L, 8000, PAYMENT_AT, null, null);

        assertThat(result.point()).isEqualTo(29);
        assertThat(result.persona()).isEqualTo(CostPersona.DELIVERY);
    }

    private User userWithPersona(CostPersona persona) {
        User user = User.of("a@b.com", "pw");
        user.changePersona(persona);
        return user;
    }
}
