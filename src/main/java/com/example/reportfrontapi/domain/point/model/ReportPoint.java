package com.example.reportfrontapi.domain.point.model;

import com.example.reportfrontapi.common.converter.PointAmountDivisionConverter;
import com.example.reportfrontapi.common.converter.PointReasonConverter;
import com.example.reportfrontapi.common.converter.PointRefTypeConverter;
import com.example.reportfrontapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 포인트 원장(단일 진실원). 잔액 = 사용자별 (적립 +금액 / 차감 −금액) 합계.
 * 모든 포인트 이동(적립/차감/조정/환불)을 한 줄씩 기록한다.
 * 분류/사유/출처유형은 CodeEnum(code)로 저장한다.
 */
@Entity
@Table(name = "REPORT_POINT")
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ReportPoint extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_point_id", nullable = false)
    private Long reportPointId;  // 원장 일련번호

    @Column(name = "user_id", nullable = false)
    private Long userId;    // 소유 사용자 ID

    @Convert(converter = PointAmountDivisionConverter.class)
    @Column(name = "point_amt_div", length = 8, nullable = false)
    private PointAmountDivision pointAmountDivision;  // 적립/차감 구분

    @Column(name = "point_amt", nullable = false)
    private Integer pointAmount;    // 포인트 금액(양수 크기)

    @Convert(converter = PointReasonConverter.class)
    @Column(name = "reason", length = 8, nullable = false)
    private PointReason reason;  // 이동 사유

    @Convert(converter = PointRefTypeConverter.class)
    @Column(name = "ref_type", length = 8)
    private PointRefType refType;  // 출처 유형(REPORT_COST / REDEMPTION_ORDER 등)

    @Column(name = "ref_id")
    private Long refId;  // 출처 식별자

    // 부호 있는 증감(적립 +, 차감 −).
    public int signedAmount() {
        return PointAmountDivision.INCREASE.equals(pointAmountDivision) ? pointAmount : -pointAmount;
    }

    // 부호 있는 delta로 원장 항목 생성(양수=적립, 음수=차감).
    public static ReportPoint of(Long userId, int delta, PointReason reason, PointRefType refType, Long refId) {
        PointAmountDivision division = delta >= 0 ? PointAmountDivision.INCREASE : PointAmountDivision.DECREASE;
        return ReportPoint.builder()
                .userId(userId)
                .pointAmountDivision(division)
                .pointAmount(Math.abs(delta))
                .reason(reason)
                .refType(refType)
                .refId(refId)
                .build();
    }
}
