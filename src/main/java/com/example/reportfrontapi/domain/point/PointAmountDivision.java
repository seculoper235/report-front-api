package com.example.reportfrontapi.domain.point;

import com.example.reportfrontapi.common.code.CodeEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PointAmountDivision implements CodeEnum {
    INCREASE("RE030001", "적립"),
    DECREASE("RE030002", "차감");

    private final String code;
    private final String name;
}
