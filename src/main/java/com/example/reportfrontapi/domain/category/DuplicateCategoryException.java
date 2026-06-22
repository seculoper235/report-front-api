package com.example.reportfrontapi.domain.category;

/**
 * 같은 사용자가 이미 가진 이름으로 카테고리를 추가하려 할 때.
 */
public class DuplicateCategoryException extends RuntimeException {
    public DuplicateCategoryException(String categoryName) {
        super("이미 사용 중인 카테고리입니다: " + categoryName);
    }
}
