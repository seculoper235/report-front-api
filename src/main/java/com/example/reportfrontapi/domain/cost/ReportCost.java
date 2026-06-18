package com.example.reportfrontapi.domain.cost;

import com.example.reportfrontapi.common.dto.Yn;
import com.example.reportfrontapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "\"RPT_COST\"")
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ReportCost extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "rpt_cost_id", nullable = false)
    private Long reportCostId;  // 레포트 코스트 일련번호

    @Column(name = "cat_nm", length = 20, nullable = false)
    private String categoryName;    // 카테고리 이름

    @Column(name = "cost_nm", length = 20, nullable = false)
    private String costName;    // 코스트 이름

    @Enumerated(EnumType.STRING)
    @Column(name = "fixed_yn", nullable = false)
    private Yn fixedYn;    // 고정 지출 여부

    @Column(name = "cost_desc")
    private String costDescription;    // 코스트 상세

    @Column(name = "cost_amt", nullable = false)
    private Long costAmount;    // 코스트 금액

    @Convert(converter = PaymentMethodConverter.class)
    @Column(name = "cost_mtd", length = 8, nullable = false)
    private PaymentMethod paymentMethod;    // 지출 수단

    @Column(name = "pay_at", nullable = false)
    private LocalDateTime paymentAt;    // 지출 일시

    @Enumerated(EnumType.STRING)
    @Column(name = "cost_dvsn", length = 8)
    private CostDivision costDivision;    // 소비 유형

    @Column(name = "cost_pnt")
    private Integer costPoint;    // 소비 포인트

    public Integer getCostPoint() {
        if (costDivision == null || costPoint == null) {
            return 0;
        }
        return costPoint;
    }

    public Integer getNormalCostPoint() {
        if (costDivision == null || costPoint == null) {
            return 0;
        }
        return CostDivision.GOOD.equals(costDivision) ? costPoint : costPoint * -1;
    }

    public void update(String categoryName, String costName, Yn fixedYn, String costDescription,
                       Long costAmount, PaymentMethod paymentMethod, LocalDateTime paymentAt,
                       CostDivision costDivision, Integer costPoint) {
        this.categoryName = categoryName;
        this.costName = costName;
        this.fixedYn = fixedYn;
        this.costDescription = costDescription;
        this.costAmount = costAmount;
        this.paymentMethod = paymentMethod;
        this.paymentAt = paymentAt;
        this.costDivision = costDivision;
        this.costPoint = costPoint;
    }
}
