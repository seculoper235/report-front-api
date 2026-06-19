package com.example.reportfrontapi.domain.auth;

/**
 * refresh/access 토큰이 유효하지 않거나 저장값과 일치하지 않을 때.
 */
public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
