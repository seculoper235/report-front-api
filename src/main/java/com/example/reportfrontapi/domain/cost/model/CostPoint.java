package com.example.reportfrontapi.domain.cost.model;

import com.example.reportfrontapi.common.entity.BaseEntity;
import com.example.reportfrontapi.domain.user.model.CostPersona;
import jakarta.persistence.*;
import lombok.*;

/**
 * 소비 1건의 계산된 포인트(서버 소유·파생). 지출 내역(ReportCost)과 1:1.
 * 소비 습관(division)은 ReportCost의 costDivision과 동일한 스냅샷이며 변경되지 않는다.
 * 금액 수정으로 포인트만 바뀔 수 있어 pointAmount는 갱신 가능하다.
 */
@Entity
@Table(name = "RPT_COST_POINT")
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CostPoint extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rpt_cost_point_id", nullable = false)
    private Long costPointId;

    @Column(name = "user_id", nullable = false)
    private Long userId;    // 소유 사용자 ID

    @Column(name = "rpt_cost_id", nullable = false)
    private Long reportCostId;    // 대상 소비 내역 (1:1)

    @Enumerated(EnumType.STRING)
    @Column(name = "cost_dvsn", length = 8, nullable = false)
    private CostDivision division;    // GOOD=적립 / BAD=차감 (습관 스냅샷)

    @Column(name = "cost_pnt", nullable = false)
    private Integer pointAmount;    // 계산된 포인트(양수 크기)

    @Enumerated(EnumType.STRING)
    @Column(name = "persona", length = 20)
    private CostPersona persona;    // 차감 계산 시점 페르소나 스냅샷(GOOD은 null)

    // 부호 있는 순포인트(GOOD +, BAD −).
    public int signedPoint() {
        return CostDivision.GOOD.equals(division) ? pointAmount : -pointAmount;
    }

    public static CostPoint of(Long userId, Long reportCostId, CostDivision division,
                               int pointAmount, CostPersona persona) {
        return CostPoint.builder()
                .userId(userId)
                .reportCostId(reportCostId)
                .division(division)
                .pointAmount(pointAmount)
                .persona(persona)
                .build();
    }

    // 소비 수정으로 포인트 재계산 시 크기만 갱신(습관/페르소나 스냅샷은 유지).
    public void updatePointAmount(int pointAmount) {
        this.pointAmount = pointAmount;
    }
}
