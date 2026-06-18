package com.example.reportfrontapi.domain.cost;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ReportCost {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "report_cost_id", nullable = false)
    private Long reportCostId;  // 레포트 코스트 일련번호

    @Column(name = "cost_nm", nullable = false)
    private String category;    // 카테고리

    @Column(name = "cost_nm", nullable = false)
    private String costName;    // 코스트 이름

    @Column(name = "cost_desc")
    private String costDescription;    // 코스트 상세

    @Column(name = "cost_amt", nullable = false)
    private Long costAmount;    // 코스트 금액

    @Column(name = "cost_mtd", nullable = false)
    private String costMethod;    // 지출 수단

    @Column(name = "pay_at", nullable = false)
    private LocalDateTime paymentAt;    // 지출 일시

    @Column(name = "cost_mtd")
    private CostDivision costDivision;    // 소비 유형

    @Column(name = "cost_amt")
    private Integer costPoint;    // 소비 포인트

    public Integer getCostPoint() {
        return CostDivision.GOOD.equals(costDivision) ? costPoint : costPoint * -1;
    }
}
