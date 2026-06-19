package com.example.reportfrontapi.domain.point.application;

import com.example.reportfrontapi.domain.point.PointReason;
import com.example.reportfrontapi.domain.point.ReportPoint;
import com.example.reportfrontapi.domain.point.repository.ReportPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 포인트 원장(report_point) 기반 잔액/적립/차감 처리. 단일 진실원.
 * 잔액 = 사용자별 delta 합계.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointService {

    public static final String REF_REPORT_COST = "REPORT_COST";
    public static final String REF_REDEMPTION_ORDER = "REDEMPTION_ORDER";

    private final ReportPointRepository reportPointRepository;

    public int getBalance(Long userId) {
        Integer sum = reportPointRepository.sumByUserId(userId);
        return sum != null ? sum : 0;
    }

    public List<PointLedgerResponse> getLedger(Long userId) {
        return reportPointRepository.findByUserId(userId).stream()
                .map(PointLedgerResponse::from)
                .toList();
    }

    // 소비 등록 적립(0이면 기록 생략).
    @Transactional
    public void recordEarnCost(Long userId, int delta, Long costId) {
        record(userId, delta, PointReason.EARN_COST, REF_REPORT_COST, costId);
    }

    // 소비 수정/삭제 등으로 인한 포인트 조정(0이면 기록 생략).
    @Transactional
    public void recordAdjust(Long userId, int delta, String refType, Long refId) {
        record(userId, delta, PointReason.ADJUST, refType, refId);
    }

    // 교환 차감.
    @Transactional
    public void recordRedeem(Long userId, int pointCost, Long orderId) {
        record(userId, -pointCost, PointReason.REDEEM, REF_REDEMPTION_ORDER, orderId);
    }

    private void record(Long userId, int delta, PointReason reason, String refType, Long refId) {
        if (delta == 0) {
            return;
        }
        reportPointRepository.save(ReportPoint.of(userId, delta, reason, refType, refId));
    }
}
