package com.example.reportfrontapi.domain.point;

import com.example.reportfrontapi.common.code.CodeEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PointRefType implements CodeEnum {
    REPORT_COST("RE020001", "소비 등록 적립"),
    REDEMPTION_ORDER("RE020002", "기프티콘 교환 차감");

    private final String code;
    private final String name;
}
