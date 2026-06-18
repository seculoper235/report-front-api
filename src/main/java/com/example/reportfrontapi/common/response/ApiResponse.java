package com.example.reportfrontapi.common.response;

public record ApiResponse<T>(
        String code,     // 응답 코드
        String message,  // 응답 메시지
        T body           // 응답 본문
) {
    public static <T> ApiResponse<T> success(T body) {
        return of(ResponseCode.SUCCESS, body);
    }

    public static <T> ApiResponse<T> of(ResponseCode responseCode, T body) {
        return new ApiResponse<>(responseCode.getCode(), responseCode.getName(), body);
    }

    public static <T> ApiResponse<T> of(ResponseCode responseCode, String message, T body) {
        return new ApiResponse<>(responseCode.getCode(), message, body);
    }
}
