package com.example.reportfrontapi.common.code;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * code/name 쌍으로 구성된 enum의 공통 타입.
 * enum은 클래스를 상속할 수 없으므로 인터페이스로 정의한다.
 *
 * JSON 직렬화/역직렬화는 code 값을 기준으로 한다(code <-> CodeEnum).
 * getCode()에 @JsonValue를 두어 직렬화 시 code를 쓰고, 역직렬화 시에도 code로 매칭한다.
 */
public interface CodeEnum {
    @JsonValue
    String getCode();

    String getName();

    /**
     * code 값으로 해당 enum 상수를 조회한다. (enum 기본 valueOf의 code 버전)
     *
     * @param type 조회 대상 enum 타입
     * @param code 찾을 코드 값
     * @return code가 일치하는 enum 상수
     * @throws IllegalArgumentException 일치하는 코드가 없을 경우
     */
    static <E extends Enum<E> & CodeEnum> E fromCode(Class<E> type, String code) {
        return Arrays.stream(type.getEnumConstants())
                .filter(constant -> constant.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown code: " + code + " for " + type.getSimpleName()));
    }
}
