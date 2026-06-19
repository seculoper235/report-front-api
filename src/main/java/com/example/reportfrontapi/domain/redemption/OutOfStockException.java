package com.example.reportfrontapi.domain.redemption;

/**
 * 해당 상품의 미사용(AVAILABLE) 코드 재고가 없을 때.
 */
public class OutOfStockException extends RuntimeException {
    public OutOfStockException(Long productId) {
        super("재고가 없습니다. productId=" + productId);
    }
}
