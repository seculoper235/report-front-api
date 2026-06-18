package com.example.reportfrontapi.domain.cost;

import com.example.reportfrontapi.common.code.CodeEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod implements CodeEnum {
    CASH("RP010001", "현금"),
    CARD("RP010002", "신용카드"),
    CREDIT_CARD("RP010003", "체크카드"),
    ACCOUNT_TRANSFER("RP010004", "계좌이체");

    private final String code;
    private final String name;
}
