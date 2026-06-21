package com.example.reportfrontapi.domain.product.application;

import com.example.reportfrontapi.domain.gift.repository.GiftInventoryRepository;
import com.example.reportfrontapi.domain.product.controller.dto.ProductFindResponse;
import com.example.reportfrontapi.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductFindService {

    private final ProductRepository productRepository;
    private final GiftInventoryRepository giftInventoryRepository;

    // 노출 상품 목록 + 상품별 재고 보유 여부.
    public List<ProductFindResponse> findAll() {
        return productRepository.findAllActive().stream()
                .map(p -> ProductFindResponse.from(p, giftInventoryRepository.countAvailable(p.getProductId()) > 0))
                .toList();
    }
}
