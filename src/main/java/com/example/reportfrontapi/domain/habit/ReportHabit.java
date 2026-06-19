package com.example.reportfrontapi.domain.habit;

import com.example.reportfrontapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "RPT_HABIT")
@Getter
@Setter
@NoArgsConstructor
public class ReportHabit extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "report_habt_id", nullable = false)
    private Long reportCostId;  // 레포트 습관 일련번호

    @Column(name = "habt_nm", nullable = false)
    private String habitName;    // 습관 이름

    @Enumerated(EnumType.STRING)
    @Column(name = "habt_dvsn")
    private HabitDivision habitDivision;    // 습관 유형

    @Column(name = "habt_pnt")
    private Integer habitPoint;    // 습관 포인트

    public Integer getHabitPoint() {
        if (habitDivision == null || habitPoint == null) {
            return 0;
        }
        return habitPoint;
    }

    public Integer getNormalHabitPoint() {
        if (habitDivision == null || habitPoint == null) {
            return 0;
        }
        return HabitDivision.GOOD.equals(habitDivision) ? habitPoint : habitPoint * -1;
    }

    public void update(String habitName, HabitDivision habitDivision, Integer habitPoint) {
        this.habitName = habitName;
        this.habitDivision = habitDivision;
        this.habitPoint = habitPoint;
    }
}
