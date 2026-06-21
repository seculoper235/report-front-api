package com.example.reportfrontapi.domain.product.application;

import com.example.reportfrontapi.domain.gift.model.GiftInventory;
import com.example.reportfrontapi.domain.gift.repository.GiftInventoryRepository;
import com.example.reportfrontapi.domain.product.Product;
import com.example.reportfrontapi.domain.product.application.dto.CodeLoadRequest;
import com.example.reportfrontapi.domain.product.application.dto.ProductCreateRequest;
import com.example.reportfrontapi.domain.product.application.dto.ProductResponse;
import com.example.reportfrontapi.domain.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final GiftInventoryRepository giftInventoryRepository;

    // 노출 상품 목록 + 상품별 재고 보유 여부.
    public List<ProductResponse> findAll() {
        return productRepository.findAllActive().stream()
                .map(p -> ProductResponse.from(p, giftInventoryRepository.countAvailable(p.getProductId()) > 0))
                .toList();
    }

    // 운영자: 상품 등록.
    @Transactional
    public Long create(ProductCreateRequest request) {
        Product product = productRepository.save(
                Product.of(request.name(), request.brand(), request.imageUrl(), request.pointCost()));
        return product.getProductId();
    }

    // 운영자: 코드 재고 적재. 적재된 코드 수를 반환.
    @Transactional
    public int addCodes(Long productId, CodeLoadRequest request) {
        productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

        for (CodeLoadRequest.CodeItem item : request.codes()) {
            giftInventoryRepository.save(
                    GiftInventory.of(productId, item.code(), item.barcodeImageUrl(), item.validUntil()));
        }
        return request.codes().size();
    }
}
