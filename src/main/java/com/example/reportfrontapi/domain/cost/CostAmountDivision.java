package com.example.reportfrontapi.domain.cost;

import com.example.reportfrontapi.common.code.CodeEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CostAmountDivision implements CodeEnum {
    INCREASE("RP020001", "증가"),
    DECREASE("RP020002", "감소");

    private final String code;
    private final String name;
}
