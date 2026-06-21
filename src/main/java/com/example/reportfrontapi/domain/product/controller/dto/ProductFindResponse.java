package com.example.reportfrontapi.domain.product.controller.dto;

import com.example.reportfrontapi.domain.product.model.Product;

public record ProductFindResponse(
        Long productId,
        String name,
        String brand,
        String imageUrl,
        Integer pointCost,
        boolean inStock        // 미사용 코드 재고 보유 여부
) {
    public static ProductFindResponse from(Product product, boolean inStock) {
        return new ProductFindResponse(
                product.getProductId(),
                product.getName(),
                product.getBrand(),
                product.getImageUrl(),
                product.getPointCost(),
                inStock);
    }
}
