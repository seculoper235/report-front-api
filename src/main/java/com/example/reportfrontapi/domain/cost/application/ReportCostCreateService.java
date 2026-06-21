package com.example.reportfrontapi.domain.cost.application;

import com.example.reportfrontapi.domain.cost.controller.dto.ReportCostCreateRequest;
import com.example.reportfrontapi.domain.cost.controller.dto.ReportCostCreateResponse;
import com.example.reportfrontapi.domain.category.model.CostCategory;
import com.example.reportfrontapi.domain.cost.model.ReportCost;
import com.example.reportfrontapi.domain.category.repository.CostCategoryRepository;
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
public class ReportCostCreateService {
    private final ReportCostRepository reportCostRepository;
    private final CostCategoryRepository costCategoryRepository;
    private final PointCreateService pointCreateService;

    @Transactional
    public ReportCostCreateResponse create(ReportCostCreateRequest request) {
        ReportCost cost = ReportCost.builder()
                .userId(SecurityUtil.getRequiredCurrentUserId())
                .category(getCategoryOrThrow(request.categoryId()))
                .costName(request.costName())
                .fixedYn(request.fixedYn())
                .costDescription(request.costDescription())
                .amountDivision(request.amountDivision())
                .costAmount(request.costAmount())
                .paymentMethod(request.paymentMethod())
                .paymentAt(request.paymentAt())
                .costDivision(request.costDivision())
                .costPoint(request.costPoint())
                .build();

        ReportCost saved = reportCostRepository.save(cost);
        // 적립: 순포인트(GOOD +, BAD −)를 원장에 기록
        pointCreateService.recordEarnCost(saved.getUserId(), saved.getNormalCostPoint(), saved.getReportCostId());
        return ReportCostCreateResponse.from(saved);
    }

    @Transactional
    public ReportCostCreateResponse update(Long id, ReportCostCreateRequest request) {
        ReportCost cost = getOrThrow(id);
        int oldNetPoint = cost.getNormalCostPoint();
        cost.update(
                getCategoryOrThrow(request.categoryId()),
                request.costName(),
                request.fixedYn(),
                request.costDescription(),
                request.amountDivision(),
                request.costAmount(),
                request.paymentMethod(),
                request.paymentAt(),
                request.costDivision(),
                request.costPoint()
        );

        // 조정: 변경된 순포인트 차액만 원장에 기록
        pointCreateService.recordAdjust(cost.getUserId(), cost.getNormalCostPoint() - oldNetPoint,
                PointRefType.REPORT_COST, id);
        return ReportCostCreateResponse.from(cost);
    }

    private ReportCost getOrThrow(Long id) {
        return reportCostRepository.findByIdAndOwner(id, SecurityUtil.getRequiredCurrentUserId())
                .orElseThrow(() -> new EntityNotFoundException("ReportCost not found: " + id));
    }

    private CostCategory getCategoryOrThrow(Long categoryId) {
        return costCategoryRepository.findByIdAndOwner(categoryId, SecurityUtil.getRequiredCurrentUserId())
                .orElseThrow(() -> new EntityNotFoundException("CostCategory not found: " + categoryId));
    }
}
