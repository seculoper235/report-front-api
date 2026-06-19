package com.example.reportfrontapi.domain.point.application;

import com.example.reportfrontapi.domain.cost.repository.ReportCostRepository;
import com.example.reportfrontapi.domain.redemption.repository.RedemptionOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 포인트 잔액 = cost 순포인트 합계 − 교환(ISSUED) 차감 합계.
 * (MVP 최소안: 별도 원장 없이 파생 계산. 단일 진실원은 서버.)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointService {

    private final ReportCostRepository reportCostRepository;
    private final RedemptionOrderRepository redemptionOrderRepository;

    public int getBalance(Long userId) {
        int earned = nz(reportCostRepository.sumNetPoint(userId));
        int spent = nz(redemptionOrderRepository.sumIssuedPointCost(userId));
        return earned - spent;
    }

    private int nz(Integer value) {
        return value != null ? value : 0;
    }
}
