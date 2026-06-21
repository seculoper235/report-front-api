package com.example.reportfrontapi.domain.gift.model;

public enum GiftInventoryStatus {
    AVAILABLE,  // 미사용(지급 가능)
    RESERVED,   // 예약(지급 처리 중)
    ISSUED      // 지급 완료
}
