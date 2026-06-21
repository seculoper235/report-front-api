package com.example.reportfrontapi.domain.product.model;

import com.example.reportfrontapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "RPT_PRODUCT")
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Product extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rpt_product_id", nullable = false)
    private Long productId;  // 상품 일련번호

    @Column(name = "name", length = 100, nullable = false)
    private String name;    // 상품명

    @Column(name = "brand", length = 100)
    private String brand;   // 브랜드

    @Column(name = "image_url", length = 500)
    private String imageUrl;    // 이미지 URL

    @Column(name = "point_cost", nullable = false)
    private Integer pointCost;   // 교환에 필요한 포인트

    @Column(name = "active", nullable = false)
    private boolean active;  // 노출 여부

    public static Product of(String name, String brand, String imageUrl, Integer pointCost) {
        return Product.builder()
                .name(name)
                .brand(brand)
                .imageUrl(imageUrl)
                .pointCost(pointCost)
                .active(true)
                .build();
    }
}
