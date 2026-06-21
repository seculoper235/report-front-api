package com.example.reportfrontapi.domain.point.application;

import com.example.reportfrontapi.domain.point.model.PointReason;
import com.example.reportfrontapi.domain.point.model.PointRefType;
import com.example.reportfrontapi.domain.point.model.ReportPoint;
import com.example.reportfrontapi.domain.point.repository.ReportPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 포인트 원장(report_point) 적립/차감/조정 기록. 단일 진실원.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointCreateService {
    private final ReportPointRepository reportPointRepository;

    // 소비 등록 적립(0이면 기록 생략).
    @Transactional
    public void recordEarnCost(Long userId, int delta, Long costId) {
        record(userId, delta, PointReason.EARN_COST, PointRefType.REPORT_COST, costId);
    }

    // 소비 수정/삭제 등으로 인한 포인트 조정(0이면 기록 생략).
    @Transactional
    public void recordAdjust(Long userId, int delta, PointRefType refType, Long refId) {
        record(userId, delta, PointReason.ADJUST, refType, refId);
    }

    // 교환 차감.
    @Transactional
    public void recordRedeem(Long userId, int pointCost, Long orderId) {
        record(userId, -pointCost, PointReason.REDEEM, PointRefType.REDEMPTION_ORDER, orderId);
    }

    private void record(Long userId, int delta, PointReason reason, PointRefType refType, Long refId) {
        if (delta == 0) {
            return;
        }
        reportPointRepository.save(ReportPoint.of(userId, delta, reason, refType, refId));
    }
}
