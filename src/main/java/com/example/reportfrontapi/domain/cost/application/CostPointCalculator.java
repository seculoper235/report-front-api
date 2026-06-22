package com.example.reportfrontapi.domain.cost.application;

import com.example.reportfrontapi.domain.category.repository.CostCategoryRepository;
import com.example.reportfrontapi.domain.cost.model.CostDivision;
import com.example.reportfrontapi.domain.cost.repository.CostPointRepository;
import com.example.reportfrontapi.domain.cost.repository.ReportCostRepository;
import com.example.reportfrontapi.domain.point.policy.BadSpendingContext;
import com.example.reportfrontapi.domain.point.policy.BadSpendingPolicyResolver;
import com.example.reportfrontapi.domain.point.policy.GoodSpendingPolicy;
import com.example.reportfrontapi.domain.user.application.UserPersonaService;
import com.example.reportfrontapi.domain.user.model.CostPersona;
import com.example.reportfrontapi.domain.user.model.User;
import com.example.reportfrontapi.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 소비 1건의 포인트(양수 크기)를 서버에서 계산한다. 클라가 보낸 포인트는 신뢰하지 않으며,
 * GOOD은 {@link GoodSpendingPolicy}, BAD는 페르소나별 {@link BadSpendingPolicyResolver}로 산출한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CostPointCalculator {
    // 배달러 빈도 계산 기간(일).
    private static final int DELIVERY_WINDOW_DAYS = 3;

    private final GoodSpendingPolicy goodSpendingPolicy;
    private final BadSpendingPolicyResolver badSpendingPolicyResolver;
    private final ReportCostRepository reportCostRepository;
    private final CostPointRepository costPointRepository;
    private final CostCategoryRepository costCategoryRepository;
    private final UserRepository userRepository;

    /** 계산 결과: 포인트 크기 + 차감 계산에 쓰인 페르소나 스냅샷(GOOD은 null). */
    public record Result(int point, CostPersona persona) {
        static final Result NONE = new Result(0, null);
    }

    /**
     * 차감/적립 포인트(양수 크기)를 계산한다.
     *
     * @param excludeCostId 수정 시 자기 자신 ID(당일/빈도 집계에서 제외). 신규 등록이면 null.
     * @return 포인트 크기와 페르소나. 소비유형 미선택이거나(BAD) 페르소나 미선택이면 0/null.
     */
    public Result calculate(Long userId, CostDivision division, Long categoryId, long amount,
                            LocalDateTime paymentAt, Integer requestedPoint, Long excludeCostId) {
        if (division == null) {
            return Result.NONE;
        }

        LocalDateTime dayStart = paymentAt.toLocalDate().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);

        if (division == CostDivision.GOOD) {
            int todayAccumulated = costPointRepository.sumGoodPointByPaymentDay(userId, dayStart, dayEnd);
            return new Result(goodSpendingPolicy.award(requestedPoint, todayAccumulated), null);
        }

        CostPersona persona = personaOf(userId);
        if (persona == null) {
            return Result.NONE; // 페르소나 미선택 → 차감 보류
        }

        int dailyBadCount = (int) reportCostRepository.countBadByDay(userId, dayStart, dayEnd, excludeCostId) + 1;
        double deliveryAverage = deliveryDailyAverage(userId, categoryId, paymentAt, excludeCostId);
        int point = badSpendingPolicyResolver.deduct(persona,
                new BadSpendingContext(amount, dailyBadCount, deliveryAverage));
        return new Result(point, persona);
    }

    // 최근 3일 배달 주문 수 ÷ 3 (1일 평균). 이번 건이 배달 카테고리면 포함한다.
    private double deliveryDailyAverage(Long userId, Long categoryId, LocalDateTime paymentAt, Long excludeCostId) {
        Long deliveryCategoryId = costCategoryRepository
                .findIdByNameAndOwner(UserPersonaService.DELIVERY_CATEGORY_NAME, userId)
                .orElse(null);
        if (deliveryCategoryId == null) {
            return 0.0;
        }

        LocalDateTime windowEnd = paymentAt.toLocalDate().plusDays(1).atStartOfDay();
        LocalDateTime windowStart = windowEnd.minusDays(DELIVERY_WINDOW_DAYS);
        long count = reportCostRepository
                .countBadByCategoryAndRange(userId, deliveryCategoryId, windowStart, windowEnd, excludeCostId);
        if (deliveryCategoryId.equals(categoryId)) {
            count += 1; // 이번 건 포함
        }
        return count / (double) DELIVERY_WINDOW_DAYS;
    }

    private CostPersona personaOf(Long userId) {
        return userRepository.findById(userId).map(User::getCostPersona).orElse(null);
    }
}
