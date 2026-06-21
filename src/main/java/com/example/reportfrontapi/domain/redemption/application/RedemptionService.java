package com.example.reportfrontapi.domain.redemption.application;

import com.example.reportfrontapi.common.storage.StorageService;
import com.example.reportfrontapi.domain.gift.model.GiftInventory;
import com.example.reportfrontapi.domain.gift.repository.GiftInventoryRepository;
import com.example.reportfrontapi.domain.point.application.PointCreateService;
import com.example.reportfrontapi.domain.point.application.PointFindService;
import com.example.reportfrontapi.domain.product.Product;
import com.example.reportfrontapi.domain.product.repository.ProductRepository;
import com.example.reportfrontapi.domain.redemption.InsufficientPointException;
import com.example.reportfrontapi.domain.redemption.OutOfStockException;
import com.example.reportfrontapi.domain.redemption.RedemptionOrder;
import com.example.reportfrontapi.domain.redemption.application.dto.RedemptionResponse;
import com.example.reportfrontapi.domain.redemption.repository.RedemptionOrderRepository;
import com.example.reportfrontapi.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RedemptionService {

    private final ProductRepository productRepository;
    private final GiftInventoryRepository giftInventoryRepository;
    private final RedemptionOrderRepository redemptionOrderRepository;
    private final PointFindService pointFindService;
    private final PointCreateService pointCreateService;
    private final UserRepository userRepository;
    private final StorageService storageService;

    // 교환 처리: 멱등 체크 → 잔액 잠금/검증 → 재고 코드 pop → 차감 주문 생성. (정책 a: 재고 0이면 차감 없이 실패)
    @Transactional
    public RedemptionResponse redeem(Long userId, Long productId, String idempotencyKey) {
        // 1. 멱등 체크: 이미 처리된 주문이면 그대로 반환(중복 차감 방지)
        var existing = redemptionOrderRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return toResponse(existing.get());
        }

        // 2. 사용자 행 비관적 락(동시 교환 직렬화 → 잔액 정합성)
        userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        // 3. 상품 조회 + active 확인
        Product product = productRepository.findActiveById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

        // 4. 잔액 검증
        int balance = pointFindService.getBalance(userId);
        if (balance < product.getPointCost()) {
            throw new InsufficientPointException(balance, product.getPointCost());
        }

        // 5. 재고 코드 pop(비관적 락). 0이면 차감 없이 실패
        GiftInventory inventory = giftInventoryRepository.popAvailable(productId)
                .orElseThrow(() -> new OutOfStockException(productId));

        // 6. 차감 주문 생성 + 코드 지급 연결
        RedemptionOrder order = redemptionOrderRepository.save(
                RedemptionOrder.issued(userId, productId, product.getPointCost(),
                        idempotencyKey, inventory.getGiftInventoryId()));
        inventory.issueTo(order.getRedemptionOrderId());

        // 7. 원장에 차감 기록
        pointCreateService.recordRedeem(userId, product.getPointCost(), order.getRedemptionOrderId());

        return responseOf(order, product, inventory);
    }

    public List<RedemptionResponse> findHistory(Long userId) {
        return redemptionOrderRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    private RedemptionResponse toResponse(RedemptionOrder order) {
        Product product = productRepository.findById(order.getProductId()).orElse(null);
        GiftInventory inventory = order.getGiftInventoryId() != null
                ? giftInventoryRepository.findById(order.getGiftInventoryId()).orElse(null)
                : null;
        return responseOf(order, product, inventory);
    }

    private RedemptionResponse responseOf(RedemptionOrder order, Product product, GiftInventory inventory) {
        return new RedemptionResponse(
                order.getRedemptionOrderId(),
                order.getProductId(),
                product != null ? product.getName() : null,
                product != null ? product.getBrand() : null,
                product != null ? product.getImageUrl() : null,
                order.getPointCost(),
                order.getStatus().name(),
                inventory != null ? inventory.getCode() : null,
                // 비공개 바코드는 저장된 object key를 단기 presigned GET URL로 변환해 소유자에게만 노출.
                inventory != null ? storageService.resolveBarcodeUrl(inventory.getBarcodeImageUrl()) : null,
                inventory != null ? inventory.getValidUntil() : null,
                order.getCreatedAt());
    }
}
