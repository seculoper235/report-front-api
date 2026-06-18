package com.example.reportfrontapi.common.response;

import com.example.reportfrontapi.common.code.CodeEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseCode implements CodeEnum {
    SUCCESS("RP000000", "요청에 성공했습니다."),
    INVALID_INPUT("RP004000", "입력값이 올바르지 않습니다."),
    NOT_FOUND("RP004040", "대상을 찾을 수 없습니다."),
    INTERNAL_ERROR("RP005000", "서버 오류가 발생했습니다.");

    private final String code;
    private final String name;
}
