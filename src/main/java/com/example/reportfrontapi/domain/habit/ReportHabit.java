package com.example.reportfrontapi.domain.habit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ReportHabit {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "report_habt_id", nullable = false)
    private Long reportCostId;  // 레포트 습관 일련번호

    @Column(name = "habt_nm", nullable = false)
    private String habitName;    // 습관 이름

    @Column(name = "cost_mtd")
    private HabitDivision habitDivision;    // 습관 유형

    @Column(name = "cost_amt")
    private Integer habitPoint;    // 습관 포인트

    public Integer getHabitPoint() {
        return HabitDivision.GOOD.equals(habitDivision) ? habitPoint : habitPoint * -1;
    }
}
