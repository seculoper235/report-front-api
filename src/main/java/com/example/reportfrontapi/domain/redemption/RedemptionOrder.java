package com.example.reportfrontapi.domain.redemption;

import com.example.reportfrontapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 포인트 → 기프티콘 교환 주문. idempotency_key로 중복 차감을 막는다.
 */
@Entity
@Table(
        name = "RPT_REDEMPTION_ORDER",
        uniqueConstraints = @UniqueConstraint(name = "uk_redemption_idem", columnNames = "idempotency_key")
)
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RedemptionOrder extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "redemption_order_id", nullable = false)
    private Long redemptionOrderId; // 교환 주문 일련번호

    @Column(name = "user_id", nullable = false)
    private Long userId;    // 교환한 사용자 ID

    @Column(name = "product_id", nullable = false)
    private Long productId;  // 교환한 상품 ID

    @Column(name = "point_cost", nullable = false)
    private Integer pointCost;   // 차감 포인트(주문 시점 스냅샷)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private RedemptionStatus status; // 주문 상태

    @Column(name = "idempotency_key", length = 100, nullable = false)
    private String idempotencyKey;   // 멱등키

    @Column(name = "gift_inventory_id")
    private Long giftInventoryId;    // 지급된 재고 코드 ID

    public static RedemptionOrder issued(Long userId, Long productId, Integer pointCost,
                                         String idempotencyKey, Long giftInventoryId) {
        return RedemptionOrder.builder()
                .userId(userId)
                .productId(productId)
                .pointCost(pointCost)
                .status(RedemptionStatus.ISSUED)
                .idempotencyKey(idempotencyKey)
                .giftInventoryId(giftInventoryId)
                .build();
    }
}
