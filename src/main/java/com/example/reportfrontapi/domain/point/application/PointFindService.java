package com.example.reportfrontapi.domain.point.application;

import com.example.reportfrontapi.domain.point.controller.dto.PointLedgerFindResponse;
import com.example.reportfrontapi.domain.point.repository.ReportPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 포인트 원장(report_point) 조회. 잔액 = 사용자별 delta 합계.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointFindService {
    private final ReportPointRepository reportPointRepository;

    public int getBalance(Long userId) {
        Integer sum = reportPointRepository.sumByUserId(userId);
        return sum != null ? sum : 0;
    }

    public List<PointLedgerFindResponse> getLedger(Long userId) {
        return reportPointRepository.findByUserId(userId).stream()
                .map(PointLedgerFindResponse::from)
                .toList();
    }
}
