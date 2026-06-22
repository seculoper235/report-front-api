package com.example.reportfrontapi.domain.point.policy;

import com.example.reportfrontapi.domain.user.model.CostPersona;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 페르소나 → {@link BadSpendingPolicy} 라우팅. 등록된 모든 정책을 페르소나로 색인한다.
 */
public class BadSpendingPolicyResolver {
    private final Map<CostPersona, BadSpendingPolicy> byPersona;

    public BadSpendingPolicyResolver(List<BadSpendingPolicy> policies) {
        this.byPersona = policies.stream()
                .collect(Collectors.toMap(BadSpendingPolicy::persona, Function.identity()));
    }

    /** 페르소나에 맞는 차감 포인트(양수 크기). 등록된 정책이 없으면 예외. */
    public int deduct(CostPersona persona, BadSpendingContext ctx) {
        BadSpendingPolicy policy = byPersona.get(persona);
        if (policy == null) {
            throw new IllegalStateException("BadSpendingPolicy not found for persona: " + persona);
        }
        return policy.deduct(ctx);
    }
}
