package com.example.reportfrontapi.domain.point.application;

import com.example.reportfrontapi.domain.cost.repository.ReportCostRepository;
import com.example.reportfrontapi.domain.point.model.PointReason;
import com.example.reportfrontapi.domain.point.policy.PointPolicyProperties;
import com.example.reportfrontapi.domain.point.repository.ReportPointRepository;
import com.example.reportfrontapi.domain.user.model.User;
import com.example.reportfrontapi.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 매일 실행되어 사용자별로 기본 포인트(20)를 지급하고,
 * 전일부터 거슬러 BAD 소비가 없는 연속 일수만큼 보너스(+1씩, 최대 +7)를 추가 적립한다.
 */
@Component
@RequiredArgsConstructor
public class DailyBonusScheduler {

    private static final Logger log = LoggerFactory.getLogger(DailyBonusScheduler.class);

    private final UserRepository userRepository;
    private final ReportCostRepository reportCostRepository;
    private final ReportPointRepository reportPointRepository;
    private final PointCreateService pointCreateService;
    private final PointPolicyProperties policyProperties;

    @Scheduled(cron = "${scheduler.daily-bonus.cron}")
    @Transactional
    public void grantDailyBonus() {
        LocalDate today = LocalDate.now();
        int base = policyProperties.bonus().dailyBase();
        int maxStreakBonus = policyProperties.bonus().maxStreakBonus();

        log.info("[일일 보너스] {} 지급 시작 (기본 {}, 최대 보너스 +{})", today, base, maxStreakBonus);

        for (User user : userRepository.findAll()) {
            try {
                grantOne(user.getUserId(), today, base, maxStreakBonus);
            } catch (Exception e) {
                // 한 사용자가 실패해도 나머지 지급은 계속한다.
                log.error("[일일 보너스] 실패 - userId={}", user.getUserId(), e);
            }
        }

        log.info("[일일 보너스] {} 지급 완료", today);
    }

    private void grantOne(Long userId, LocalDate today, int base, int maxStreakBonus) {
        LocalDateTime dayStart = today.atStartOfDay();
        // 오늘 이미 지급됐다면(스케줄러 중복 실행) 건너뛴다.
        if (reportPointRepository.existsByUserReasonAndCreatedAtRange(
                userId, PointReason.DAILY_BASE, dayStart, dayStart.plusDays(1))) {
            return;
        }

        pointCreateService.recordDailyBase(userId, base);

        int streak = consecutiveCleanDays(userId, today, maxStreakBonus);
        if (streak > 0) {
            pointCreateService.recordStreakBonus(userId, streak);
        }
    }

    // 전일(어제)부터 거슬러 올라가며 BAD 소비가 0인 연속 일수. maxStreakBonus까지만 센다(보너스 상한).
    private int consecutiveCleanDays(Long userId, LocalDate today, int maxStreakBonus) {
        int streak = 0;
        for (int back = 1; back <= maxStreakBonus; back++) {
            LocalDateTime dayStart = today.minusDays(back).atStartOfDay();
            long badCount = reportCostRepository.countBadByDay(userId, dayStart, dayStart.plusDays(1), null);
            if (badCount > 0) {
                break;
            }
            streak++;
        }
        return streak;
    }
}
