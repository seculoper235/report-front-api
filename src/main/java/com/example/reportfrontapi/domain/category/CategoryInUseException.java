package com.example.reportfrontapi.domain.category;

/**
 * 소비 내역이 연결된 카테고리를 삭제하려 할 때.
 */
public class CategoryInUseException extends RuntimeException {
    public CategoryInUseException(Long categoryId) {
        super("연결된 소비 내역이 있어 삭제할 수 없는 카테고리입니다: " + categoryId);
    }
}
