package com.example.reportfrontapi.domain.gift.model;

import com.example.reportfrontapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * 기프티콘 코드 풀(재고). 교환 시 AVAILABLE 코드 1건을 pop 하여 지급한다.
 * code/pin은 민감정보이므로 로그 노출 금지.
 */
@Entity
@Table(name = "RPT_GIFT_INVENTORY")
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class GiftInventory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gift_inventory_id", nullable = false)
    private Long giftInventoryId;   // 재고 코드 일련번호

    @Column(name = "product_id", nullable = false)
    private Long productId;  // 소속 상품 ID (FK 아님)

    @Column(name = "code", length = 255, nullable = false)
    private String code;    // 쿠폰코드/핀번호 (민감정보)

    @Column(name = "barcode_image_url", length = 500)
    private String barcodeImageUrl; // 바코드 이미지 URL (선택)

    @Column(name = "valid_until")
    private LocalDate validUntil;   // 유효기간 (선택)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private GiftInventoryStatus status; // 재고 상태

    @Column(name = "redemption_order_id")
    private Long redemptionOrderId; // 지급된 교환 주문 ID (지급 전 null)

    public static GiftInventory of(Long productId, String code, String barcodeImageUrl, LocalDate validUntil) {
        return GiftInventory.builder()
                .productId(productId)
                .code(code)
                .barcodeImageUrl(barcodeImageUrl)
                .validUntil(validUntil)
                .status(GiftInventoryStatus.AVAILABLE)
                .build();
    }

    // 교환 주문에 지급 완료 처리.
    public void issueTo(Long redemptionOrderId) {
        this.status = GiftInventoryStatus.ISSUED;
        this.redemptionOrderId = redemptionOrderId;
    }
}
