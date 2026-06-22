package com.example.reportfrontapi.domain.point.policy;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 포인트 정책 계수. application.yml의 {@code point.policy.*}로 외부화해
 * 코드 수정 없이 곡선/한도/비율을 조정할 수 있게 한다.
 */
@ConfigurationProperties(prefix = "point.policy")
public record PointPolicyProperties(
        Good good,
        Delivery delivery,
        MicroSpender microSpender,
        Bonus bonus,
        Redeem redeem
) {
    /** 좋은 소비 적립: 건당 [min, max], 일 한도 dailyCap. */
    public record Good(int min, int max, int dailyCap) {
    }

    /** 배달러 금액 곡선(로그). */
    public record Delivery(LogCurve curve) {
    }

    /** 소과금러 금액 곡선(거듭제곱). */
    public record MicroSpender(PowerCurve curve) {
    }

    /** 매일 기본 포인트와 연속 무(無)나쁜소비 보너스 상한. */
    public record Bonus(int dailyBase, int maxStreakBonus) {
    }

    /** 기프티콘 교환 적립 비율(예: 0.0005 = 0.05%). */
    public record Redeem(double rate) {
    }

    /** {@code y = a·ln(x) + b}, [min, max] clip. */
    public record LogCurve(double a, double b, int min, int max) {
    }

    /** {@code y = maxPoint·(x/xMax)^exponent}, maxPoint cap. */
    public record PowerCurve(double maxPoint, double xMax, double exponent) {
    }
}
