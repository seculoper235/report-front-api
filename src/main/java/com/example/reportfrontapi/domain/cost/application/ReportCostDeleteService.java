package com.example.reportfrontapi.domain.cost.application;

import com.example.reportfrontapi.domain.cost.model.ReportCost;
import com.example.reportfrontapi.domain.cost.repository.ReportCostRepository;
import com.example.reportfrontapi.domain.point.application.PointCreateService;
import com.example.reportfrontapi.domain.point.model.PointRefType;
import com.example.reportfrontapi.web.security.SecurityUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportCostDeleteService {
    private final ReportCostRepository reportCostRepository;
    private final PointCreateService pointCreateService;

    @Transactional
    public void delete(Long id) {
        ReportCost cost = getOrThrow(id);
        // 조정: 삭제된 소비의 적립분을 원장에서 차감
        pointCreateService.recordAdjust(cost.getUserId(), -cost.getNormalCostPoint(),
                PointRefType.REPORT_COST, id);
        reportCostRepository.delete(cost);
    }

    private ReportCost getOrThrow(Long id) {
        return reportCostRepository.findByIdAndOwner(id, SecurityUtil.getRequiredCurrentUserId())
                .orElseThrow(() -> new EntityNotFoundException("ReportCost not found: " + id));
    }
}
