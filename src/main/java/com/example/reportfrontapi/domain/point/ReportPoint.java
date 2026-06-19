package com.example.reportfrontapi.domain.point;

import com.example.reportfrontapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 포인트 원장(단일 진실원). 잔액 = 사용자별 delta 합계.
 * 모든 포인트 이동(적립/차감/조정/환불)을 한 줄씩 기록한다.
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

    @Column(name = "delta", nullable = false)
    private Integer delta;  // 증감 포인트(+적립/−차감)

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", length = 20, nullable = false)
    private PointReason reason;  // 이동 사유

    @Column(name = "ref_type", length = 30)
    private String refType;  // 출처 유형(REPORT_COST / REDEMPTION_ORDER 등)

    @Column(name = "ref_id")
    private Long refId;  // 출처 식별자

    public static ReportPoint of(Long userId, int delta, PointReason reason, String refType, Long refId) {
        return ReportPoint.builder()
                .userId(userId)
                .delta(delta)
                .reason(reason)
                .refType(refType)
                .refId(refId)
                .build();
    }
}
