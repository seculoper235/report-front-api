package com.example.reportfrontapi.domain.point.policy;

/**
 * 좋은 소비(GOOD) 적립 정책.
 * 지정값(있으면 [min, max]로 clamp)이거나 미지정 시 [min, max] 균등 랜덤이며,
 * 당일 누적 적립이 일 한도(dailyCap)를 넘지 않도록 초과분을 잘라낸다.
 */
public class GoodSpendingPolicy {
    private final int min;
    private final int max;
    private final int dailyCap;
    private final PointRandomizer randomizer;

    public GoodSpendingPolicy(int min, int max, int dailyCap, PointRandomizer randomizer) {
        this.min = min;
        this.max = max;
        this.dailyCap = dailyCap;
        this.randomizer = randomizer;
    }

    /**
     * 적립 포인트를 계산한다.
     *
     * @param requested        사용자가 지정한 포인트(null이면 랜덤)
     * @param todayAccumulated 당일 이미 적립된 GOOD 포인트 합계
     */
    public int award(Integer requested, int todayAccumulated) {
        int base = (requested != null) ? clamp(requested) : randomizer.nextInRange(min, max);
        int remaining = Math.max(0, dailyCap - todayAccumulated);
        return Math.min(base, remaining);
    }

    private int clamp(int value) {
        return Math.max(min, Math.min(max, value));
    }
}
