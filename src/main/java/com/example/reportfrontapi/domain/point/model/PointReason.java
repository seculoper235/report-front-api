package com.example.reportfrontapi.domain.point.model;

import com.example.reportfrontapi.common.code.CodeEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PointReason implements CodeEnum {
    EARN_COST("RE010001", "소비 등록 적립"),
    REDEEM("RE010002", "기프티콘 교환 차감"),
    ADJUST("RE010003", "소비 수정/삭제 등 조정"),
    REFUND("RE010004", "교환 취소 환불");

    private final String code;
    private final String name;
}
