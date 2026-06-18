package com.example.reportfrontapi.domain.cost.application;

import com.example.reportfrontapi.domain.cost.ReportCost;
import com.example.reportfrontapi.domain.cost.repository.ReportCostRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportCostService {
    private final ReportCostRepository reportCostRepository;

    @Transactional
    public ReportCostResponse create(ReportCostRequest request) {
        ReportCost cost = ReportCost.builder()
                .categoryName(request.categoryName())
                .costName(request.costName())
                .fixedYn(request.fixedYn())
                .costDescription(request.costDescription())
                .costAmount(request.costAmount())
                .paymentMethod(request.paymentMethod())
                .paymentAt(request.paymentAt())
                .costDivision(request.costDivision())
                .costPoint(request.costPoint())
                .build();

        return ReportCostResponse.from(reportCostRepository.save(cost));
    }

    public List<ReportCostResponse> findAll() {
        return reportCostRepository.findAll().stream()
                .map(ReportCostResponse::from)
                .toList();
    }

    public ReportCostResponse findById(Long id) {
        return ReportCostResponse.from(getOrThrow(id));
    }

    @Transactional
    public ReportCostResponse update(Long id, ReportCostRequest request) {
        ReportCost cost = getOrThrow(id);
        cost.update(
                request.categoryName(),
                request.costName(),
                request.fixedYn(),
                request.costDescription(),
                request.costAmount(),
                request.paymentMethod(),
                request.paymentAt(),
                request.costDivision(),
                request.costPoint()
        );

        return ReportCostResponse.from(cost);
    }

    @Transactional
    public void delete(Long id) {
        ReportCost cost = getOrThrow(id);
        reportCostRepository.delete(cost);
    }

    private ReportCost getOrThrow(Long id) {
        return reportCostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ReportCost not found: " + id));
    }
}
