package com.example.reportfrontapi.domain.point.policy;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 좋은 소비 랜덤 포인트 생성기. 테스트에서 결정적 구현으로 교체할 수 있도록 분리한다.
 */
@FunctionalInterface
public interface PointRandomizer {
    /** [minInclusive, maxInclusive] 범위의 정수. */
    int nextInRange(int minInclusive, int maxInclusive);

    /** 운영 기본 구현(스레드 안전). */
    PointRandomizer THREAD_LOCAL =
            (min, max) -> ThreadLocalRandom.current().nextInt(min, max + 1);
}
