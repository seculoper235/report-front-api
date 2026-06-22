package com.example.reportfrontapi.domain.cost.model;

import com.example.reportfrontapi.common.converter.CostAmountDivisionConverter;
import com.example.reportfrontapi.common.converter.PaymentMethodConverter;
import com.example.reportfrontapi.common.dto.Yn;
import com.example.reportfrontapi.common.entity.BaseEntity;
import com.example.reportfrontapi.domain.category.model.CostCategory;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Entity
@Table(name = "RPT_COST")
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ReportCost extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rpt_cost_id", nullable = false)
    private Long reportCostId;  // 레포트 코스트 일련번호

    @Column(name = "user_id", nullable = false)
    private Long userId;    // 소유 사용자 ID (FK 아님)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rpt_cost_category_id", nullable = false)
    private CostCategory category;    // 카테고리 (RPT_COST_CAT)

    @Column(name = "cost_nm", length = 20, nullable = false)
    private String costName;    // 코스트 이름

    @Enumerated(EnumType.STRING)
    @Column(name = "fixed_yn", nullable = false)
    private Yn fixedYn;    // 고정 지출 여부

    @Column(name = "cost_desc")
    private String costDescription;    // 코스트 상세

    @Convert(converter = CostAmountDivisionConverter.class)
    @Column(name = "cost_amt_div", length = 8, nullable = false)
    private CostAmountDivision amountDivision;    // 입금/출금 구분

    @Column(name = "cost_amt", nullable = false)
    private BigInteger costAmount;    // 코스트 금액

    @Convert(converter = PaymentMethodConverter.class)
    @Column(name = "cost_mtd", length = 8, nullable = false)
    private PaymentMethod paymentMethod;    // 지출 수단

    @Column(name = "pay_at", nullable = false)
    private LocalDateTime paymentAt;    // 지출 일시

    @Enumerated(EnumType.STRING)
    @Column(name = "cost_dvsn", length = 8)
    private CostDivision costDivision;    // 소비 유형

    // 포인트는 CostPoint 테이블이 단일 진실원이다(이 엔티티에는 더 이상 저장하지 않는다).

    // 입금(INCREASE) 건의 금액(아니면 0). 입금금액 합산에 사용.
    public BigInteger getIncomeAmount() {
        if (amountDivision == null || costAmount == null) {
            return BigInteger.ZERO;
        }
        return CostAmountDivision.INCREASE.equals(amountDivision) ? costAmount : BigInteger.ZERO;
    }

    // 출금(DECREASE) 건의 금액(아니면 0). 출금금액 합산에 사용.
    public BigInteger getExpenseAmount() {
        if (amountDivision == null || costAmount == null) {
            return BigInteger.ZERO;
        }
        return CostAmountDivision.DECREASE.equals(amountDivision) ? costAmount : BigInteger.ZERO;
    }

    public void update(CostCategory category, String costName, Yn fixedYn, String costDescription,
                       CostAmountDivision amountDivision, BigInteger costAmount, PaymentMethod paymentMethod,
                       LocalDateTime paymentAt, CostDivision costDivision) {
        this.category = category;
        this.costName = costName;
        this.fixedYn = fixedYn;
        this.costDescription = costDescription;
        this.amountDivision = amountDivision;
        this.costAmount = costAmount;
        this.paymentMethod = paymentMethod;
        this.paymentAt = paymentAt;
        this.costDivision = costDivision;
    }
}
