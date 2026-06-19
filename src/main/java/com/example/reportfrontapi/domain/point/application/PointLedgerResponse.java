package com.example.reportfrontapi.domain.point.application;

import com.example.reportfrontapi.domain.point.PointAmountDivision;
import com.example.reportfrontapi.domain.point.PointReason;
import com.example.reportfrontapi.domain.point.PointRefType;
import com.example.reportfrontapi.domain.point.ReportPoint;

import java.time.LocalDateTime;

public record PointLedgerResponse(
        Long id,
        PointAmountDivision amountDivision,  // 적립/차감 (code 직렬화)
        Integer pointAmount,                 // 포인트 금액(양수 크기)
        PointReason reason,                  // 사유 (code 직렬화)
        PointRefType refType,                // 출처 유형 (code 직렬화)
        Long refId,
        LocalDateTime createdAt
) {
    public static PointLedgerResponse from(ReportPoint point) {
        return new PointLedgerResponse(
                point.getReportPointId(),
                point.getPointAmountDivision(),
                point.getPointAmount(),
                point.getReason(),
                point.getRefType(),
                point.getRefId(),
                point.getCreatedAt());
    }
}
