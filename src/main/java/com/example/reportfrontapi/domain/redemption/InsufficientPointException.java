package com.example.reportfrontapi.domain.redemption;

/**
 * 잔액이 상품 교환 포인트보다 적을 때.
 */
public class InsufficientPointException extends RuntimeException {
    public InsufficientPointException(int balance, int required) {
        super("포인트가 부족합니다. (잔액: " + balance + ", 필요: " + required + ")");
    }
}
