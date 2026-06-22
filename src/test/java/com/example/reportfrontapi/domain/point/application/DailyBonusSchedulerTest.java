package com.example.reportfrontapi.domain.point.application;

import com.example.reportfrontapi.domain.cost.repository.ReportCostRepository;
import com.example.reportfrontapi.domain.point.model.PointReason;
import com.example.reportfrontapi.domain.point.policy.PointPolicyProperties;
import com.example.reportfrontapi.domain.point.repository.ReportPointRepository;
import com.example.reportfrontapi.domain.user.model.User;
import com.example.reportfrontapi.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DailyBonusSchedulerTest {
    private static final Long USER_ID = 1L;
    private static final int BASE = 20;
    private static final int MAX_STREAK = 7;

    @Mock UserRepository userRepository;
    @Mock ReportCostRepository reportCostRepository;
    @Mock ReportPointRepository reportPointRepository;
    @Mock PointCreateService pointCreateService;

    @InjectMocks DailyBonusScheduler scheduler;

    @BeforeEach
    void setUp() {
        // PointPolicyProperties는 record라 생성자 주입이 안 되므로 직접 주입
        PointPolicyProperties props = new PointPolicyProperties(
                null, null, null, new PointPolicyProperties.Bonus(BASE, MAX_STREAK), null);
        scheduler = new DailyBonusScheduler(userRepository, reportCostRepository,
                reportPointRepository, pointCreateService, props);

        given(userRepository.findAll()).willReturn(List.of(user()));
    }

    @Test
    void 어제부터_연속_클린_일수만큼_보너스를_준다() {
        given(reportPointRepository.existsByUserReasonAndCreatedAtRange(
                eq(USER_ID), eq(PointReason.DAILY_BASE), any(), any())).willReturn(false);
        // 어제 0, 그제 0, 3일전 5(BAD) → 연속 2일
        given(reportCostRepository.countBadByDay(eq(USER_ID), any(), any(), isNull()))
                .willReturn(0L, 0L, 5L);

        scheduler.grantDailyBonus();

        verify(pointCreateService).recordDailyBase(USER_ID, BASE);
        verify(pointCreateService).recordStreakBonus(USER_ID, 2);
    }

    @Test
    void 어제_BAD가_있으면_기본만_주고_보너스는_0이다() {
        given(reportPointRepository.existsByUserReasonAndCreatedAtRange(
                eq(USER_ID), eq(PointReason.DAILY_BASE), any(), any())).willReturn(false);
        given(reportCostRepository.countBadByDay(eq(USER_ID), any(), any(), isNull()))
                .willReturn(3L); // 어제 BAD 있음

        scheduler.grantDailyBonus();

        verify(pointCreateService).recordDailyBase(USER_ID, BASE);
        verify(pointCreateService, never()).recordStreakBonus(eq(USER_ID), anyInt());
    }

    @Test
    void 연속_클린이_길어도_보너스는_7로_상한된다() {
        given(reportPointRepository.existsByUserReasonAndCreatedAtRange(
                eq(USER_ID), eq(PointReason.DAILY_BASE), any(), any())).willReturn(false);
        given(reportCostRepository.countBadByDay(eq(USER_ID), any(), any(), isNull()))
                .willReturn(0L); // 항상 클린

        scheduler.grantDailyBonus();

        verify(pointCreateService).recordStreakBonus(USER_ID, MAX_STREAK);
    }

    @Test
    void 오늘_이미_지급됐으면_건너뛴다() {
        given(reportPointRepository.existsByUserReasonAndCreatedAtRange(
                eq(USER_ID), eq(PointReason.DAILY_BASE), any(), any())).willReturn(true);

        scheduler.grantDailyBonus();

        verify(pointCreateService, never()).recordDailyBase(eq(USER_ID), anyInt());
        verify(pointCreateService, never()).recordStreakBonus(eq(USER_ID), anyInt());
    }

    private User user() {
        return User.builder().userId(USER_ID).email("a@b.com").password("pw").build();
    }
}
