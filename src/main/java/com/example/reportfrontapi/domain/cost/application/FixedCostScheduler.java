package com.example.reportfrontapi.domain.cost.application;

import com.example.reportfrontapi.common.dto.Yn;
import com.example.reportfrontapi.domain.cost.controller.dto.ReportCostCreateRequest;
import com.example.reportfrontapi.domain.cost.model.ReportCost;
import com.example.reportfrontapi.domain.cost.repository.ReportCostRepository;
import com.example.reportfrontapi.domain.user.model.Role;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 매일 자정에 실행되어, 고정 지출(fixedYn=Y)로 등록된 소비 중
 * 지출 일시의 '일(day)'이 오늘 일자와 일치하는 건을 당월 소비로 자동 등록한다.
 * 등록되는 소비의 지출 일시는 현재 년/월로 설정되며 고정 여부는 N으로 저장된다.
 */
@Component
@RequiredArgsConstructor
public class FixedCostScheduler {

    private static final Logger log = LoggerFactory.getLogger(FixedCostScheduler.class);

    private final ReportCostRepository reportCostRepository;
    private final ReportCostCreateService reportCostCreateService;

    @Scheduled(cron = "${scheduler.fixed-cost.cron}")
    public void registerFixedCosts() {
        LocalDate today = LocalDate.now();
        // 당일이 그 달의 말일이면, 당월에 존재하지 않는 일자(예: 31일)의 고정 지출도 함께 보정 등록한다.
        boolean lastDayOfMonth = today.getDayOfMonth() == today.lengthOfMonth();
        List<ReportCost> fixedCosts =
                reportCostRepository.findFixedCostsForDay(today.getDayOfMonth(), lastDayOfMonth);

        log.info("[고정 지출 등록] {} 일자 대상 {}건 처리 시작", today, fixedCosts.size());

        for (ReportCost fixedCost : fixedCosts) {
            try {
                registerOne(fixedCost, today);
            } catch (Exception e) {
                // 한 건이 실패해도 나머지 고정 지출 등록은 계속 진행한다.
                log.error("[고정 지출 등록] 실패 - reportCostId={}, userId={}",
                        fixedCost.getReportCostId(), fixedCost.getUserId(), e);
            }
        }

        log.info("[고정 지출 등록] {} 일자 처리 완료", today);
    }

    private void registerOne(ReportCost fixedCost, LocalDate today) {
        // ReportCostCreateService.create()는 SecurityContext의 사용자 ID를 사용하므로(소유자/감사자),
        // 고정 지출 소유자로 인증 컨텍스트를 설정한 뒤 등록하고, 끝나면 반드시 초기화한다.
        authenticateAs(fixedCost.getUserId());
        try {
            reportCostCreateService.create(toMonthlyCreateRequest(fixedCost, today));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    // 고정 지출을 당월 소비 등록 요청으로 변환한다. 고정 여부는 N으로 등록한다.
    // 지출 일시는 '오늘 날짜 + 원본 시각'으로 설정한다. 고정 지출은 일자가 오늘과 일치할 때만(또는
    // 말일 보정으로 오늘에 매칭될 때만) 등록되므로, 오늘 날짜를 쓰면 현재 년/월 및 말일 보정이 함께 반영된다.
    private ReportCostCreateRequest toMonthlyCreateRequest(ReportCost fixedCost, LocalDate today) {
        LocalDateTime paymentAt = today.atTime(fixedCost.getPaymentAt().toLocalTime());

        return new ReportCostCreateRequest(
                fixedCost.getCategory().getCategoryId(),
                fixedCost.getCostName(),
                Yn.N,
                fixedCost.getCostDescription(),
                fixedCost.getAmountDivision(),
                fixedCost.getCostAmount(),
                fixedCost.getPaymentMethod(),
                paymentAt,
                fixedCost.getCostDivision(),
                fixedCost.getCostPoint()
        );
    }

    private void authenticateAs(Long userId) {
        var authentication = new UsernamePasswordAuthenticationToken(
                userId, null, List.of(new SimpleGrantedAuthority("ROLE_" + Role.USER.name())));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
