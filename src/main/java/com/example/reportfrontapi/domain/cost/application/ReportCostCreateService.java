package com.example.reportfrontapi.domain.cost.application;

import com.example.reportfrontapi.domain.cost.controller.dto.ReportCostCreateRequest;
import com.example.reportfrontapi.domain.cost.controller.dto.ReportCostCreateResponse;
import com.example.reportfrontapi.domain.category.model.CostCategory;
import com.example.reportfrontapi.domain.cost.model.CostPoint;
import com.example.reportfrontapi.domain.cost.model.ReportCost;
import com.example.reportfrontapi.domain.category.repository.CostCategoryRepository;
import com.example.reportfrontapi.domain.cost.repository.CostPointRepository;
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
    private final CostPointRepository costPointRepository;
    private final CostPointCalculator costPointCalculator;
    private final PointCreateService pointCreateService;

    @Transactional
    public ReportCostCreateResponse create(ReportCostCreateRequest request) {
        Long userId = SecurityUtil.getRequiredCurrentUserId();
        CostCategory category = getCategoryOrThrow(request.categoryId());

        // 포인트는 서버가 계산한다(클라가 보낸 costPoint는 GOOD 지정값으로만 쓰인다).
        CostPointCalculator.Result result = costPointCalculator.calculate(
                userId, request.costDivision(), request.categoryId(),
                request.costAmount().longValue(), request.paymentAt(), request.costPoint(), null);

        ReportCost cost = ReportCost.builder()
                .userId(userId)
                .category(category)
                .costName(request.costName())
                .fixedYn(request.fixedYn())
                .costDescription(request.costDescription())
                .amountDivision(request.amountDivision())
                .costAmount(request.costAmount())
                .paymentMethod(request.paymentMethod())
                .paymentAt(request.paymentAt())
                .costDivision(request.costDivision())
                .build();

        ReportCost saved = reportCostRepository.save(cost);
        int signedPoint = 0;
        if (request.costDivision() != null) {
            CostPoint costPoint = costPointRepository.save(CostPoint.of(userId, saved.getReportCostId(),
                    request.costDivision(), result.point(), result.persona()));
            signedPoint = costPoint.signedPoint();
        }
        // 적립: 순포인트(GOOD +, BAD −)를 원장에 기록
        pointCreateService.recordEarnCost(saved.getUserId(), signedPoint, saved.getReportCostId());
        return ReportCostCreateResponse.from(saved, result.point());
    }

    @Transactional
    public ReportCostCreateResponse update(Long id, ReportCostCreateRequest request) {
        ReportCost cost = getOrThrow(id);
        // 소비 습관(GOOD/BAD)은 변경할 수 없다.
        if (cost.getCostDivision() != null && cost.getCostDivision() != request.costDivision()) {
            throw new IllegalArgumentException("소비 습관(GOOD/BAD)은 변경할 수 없습니다.");
        }
        CostPoint existing = costPointRepository.findByReportCostId(id, cost.getUserId()).orElse(null);
        int oldSignedPoint = existing != null ? existing.signedPoint() : 0;

        // 금액/카테고리 변경 등으로 포인트를 다시 계산한다(자기 자신은 집계에서 제외).
        CostPointCalculator.Result result = costPointCalculator.calculate(
                cost.getUserId(), request.costDivision(), request.categoryId(),
                request.costAmount().longValue(), request.paymentAt(), request.costPoint(), id);

        cost.update(
                getCategoryOrThrow(request.categoryId()),
                request.costName(),
                request.fixedYn(),
                request.costDescription(),
                request.amountDivision(),
                request.costAmount(),
                request.paymentMethod(),
                request.paymentAt(),
                request.costDivision()
        );

        int newSignedPoint = 0;
        if (existing != null) {
            existing.updatePointAmount(result.point());
            newSignedPoint = existing.signedPoint();
        } else if (request.costDivision() != null) {
            CostPoint costPoint = costPointRepository.save(CostPoint.of(cost.getUserId(), id,
                    request.costDivision(), result.point(), result.persona()));
            newSignedPoint = costPoint.signedPoint();
        }

        // 조정: 변경된 순포인트 차액만 원장에 기록
        pointCreateService.recordAdjust(cost.getUserId(), newSignedPoint - oldSignedPoint,
                PointRefType.REPORT_COST, id);
        return ReportCostCreateResponse.from(cost, result.point());
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
