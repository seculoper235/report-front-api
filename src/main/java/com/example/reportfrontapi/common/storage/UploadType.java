package com.example.reportfrontapi.common.storage;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 업로드 대상 유형. 민감도에 따라 버킷(공개/비공개)과 키 prefix가 달라진다.
 */
@Getter
@RequiredArgsConstructor
public enum UploadType {

    /** 상품 썸네일 — 공개 자산. 공개 버킷에 저장하고 공개 URL을 그대로 사용. */
    PRODUCT_IMAGE("product/", true),

    /** 바코드 이미지 — 민감정보. 비공개 버킷에 저장하고 조회 시 presigned GET으로만 노출. */
    BARCODE_IMAGE("barcode/", false);

    private final String prefix;
    private final boolean publicAsset;
}
