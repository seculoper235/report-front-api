package com.example.reportfrontapi.domain.point.policy;

/**
 * 나쁜 소비(BAD) 차감 계산에 필요한 입력값.
 *
 * @param amount                 이번 소비 금액(원)
 * @param dailyBadCount          이번 건 포함 당일 BAD 소비 개수 (소과금러 개수 성분)
 * @param deliveryDailyAverage   최근 3일 배달 주문 수 ÷ 3 (배달러 빈도 성분, 1일 평균)
 */
public record BadSpendingContext(
        long amount,
        int dailyBadCount,
        double deliveryDailyAverage
) {
}
