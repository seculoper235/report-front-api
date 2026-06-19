package com.example.reportfrontapi.domain.point.application;

import com.example.reportfrontapi.domain.point.ReportPoint;

import java.time.LocalDateTime;

public record PointLedgerResponse(
        Long id,
        Integer delta,
        String reason,
        String refType,
        Long refId,
        LocalDateTime createdAt
) {
    public static PointLedgerResponse from(ReportPoint point) {
        return new PointLedgerResponse(
                point.getReportPointId(),
                point.getDelta(),
                point.getReason().name(),
                point.getRefType(),
                point.getRefId(),
                point.getCreatedAt());
    }
}
