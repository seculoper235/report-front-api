package com.example.reportfrontapi.domain.redemption.application;

import com.example.reportfrontapi.common.storage.StorageService;
import com.example.reportfrontapi.domain.gift.model.GiftInventory;
import com.example.reportfrontapi.domain.gift.repository.GiftInventoryRepository;
import com.example.reportfrontapi.domain.product.model.Product;
import com.example.reportfrontapi.domain.product.repository.ProductRepository;
import com.example.reportfrontapi.domain.redemption.controller.dto.RedemptionFindResponse;
import com.example.reportfrontapi.domain.redemption.model.RedemptionOrder;
import com.example.reportfrontapi.domain.redemption.repository.RedemptionOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RedemptionFindService {

    private final RedemptionOrderRepository redemptionOrderRepository;
    private final ProductRepository productRepository;
    private final GiftInventoryRepository giftInventoryRepository;
    private final StorageService storageService;

    public List<RedemptionFindResponse> findHistory(Long userId) {
        return redemptionOrderRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    private RedemptionFindResponse toResponse(RedemptionOrder order) {
        Product product = productRepository.findById(order.getProductId()).orElse(null);
        GiftInventory inventory = order.getGiftInventoryId() != null
                ? giftInventoryRepository.findById(order.getGiftInventoryId()).orElse(null)
                : null;
        return responseOf(order, product, inventory);
    }

    private RedemptionFindResponse responseOf(RedemptionOrder order, Product product, GiftInventory inventory) {
        return new RedemptionFindResponse(
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
