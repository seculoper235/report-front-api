package com.example.reportfrontapi.domain.product.application.dto;

import com.example.reportfrontapi.domain.product.Product;

public record ProductResponse(
        Long productId,
        String name,
        String brand,
        String imageUrl,
        Integer pointCost,
        boolean inStock        // 미사용 코드 재고 보유 여부
) {
    public static ProductResponse from(Product product, boolean inStock) {
        return new ProductResponse(
                product.getProductId(),
                product.getName(),
                product.getBrand(),
                product.getImageUrl(),
                product.getPointCost(),
                inStock);
    }
}
