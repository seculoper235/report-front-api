package com.example.reportfrontapi.domain.category.model;

import com.example.reportfrontapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "RPT_COST_CAT")
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CostCategory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rpt_cost_category_id", nullable = false)
    private Long categoryId;

    @Column(name = "user_id", nullable = false)
    private Long userId;    // 소유 사용자 ID (FK 아님)

    @Column(name = "cat_nm", length = 20, nullable = false)
    private String categoryName;

    @Column(name = "cat_color", length = 7, nullable = false)
    private String color;    // 카테고리 색상 (#RRGGBB)

    public void rename(String categoryName) {
        this.categoryName = categoryName;
    }
}
