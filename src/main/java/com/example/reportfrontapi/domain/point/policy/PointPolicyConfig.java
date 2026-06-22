package com.example.reportfrontapi.domain.point.policy;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 포인트 정책 빈 조립. "어떤 곡선/정책을 쓰는가"를 이 한 곳에서 결정한다.
 * 곡선을 바꾸려면 여기서 구현 클래스를 교체하거나, application.yml의 계수만 바꾸면 된다.
 */
@Configuration
@EnableConfigurationProperties(PointPolicyProperties.class)
public class PointPolicyConfig {

    @Bean
    public BadSpendingPolicy deliveryBadPolicy(PointPolicyProperties props) {
        PointPolicyProperties.LogCurve c = props.delivery().curve();
        return new DeliveryBadPolicy(new LogDeductionCurve(c.a(), c.b(), c.min(), c.max()));
    }

    @Bean
    public BadSpendingPolicy microSpenderBadPolicy(PointPolicyProperties props) {
        PointPolicyProperties.PowerCurve c = props.microSpender().curve();
        return new MicroSpenderBadPolicy(new PowerDeductionCurve(c.maxPoint(), c.xMax(), c.exponent()));
    }

    @Bean
    public BadSpendingPolicy jackpotBadPolicy() {
        return new JackpotBadPolicy();
    }

    @Bean
    public BadSpendingPolicyResolver badSpendingPolicyResolver(List<BadSpendingPolicy> policies) {
        return new BadSpendingPolicyResolver(policies);
    }

    @Bean
    public GoodSpendingPolicy goodSpendingPolicy(PointPolicyProperties props) {
        PointPolicyProperties.Good g = props.good();
        return new GoodSpendingPolicy(g.min(), g.max(), g.dailyCap(), PointRandomizer.THREAD_LOCAL);
    }
}
