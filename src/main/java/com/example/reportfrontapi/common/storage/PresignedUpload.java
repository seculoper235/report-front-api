package com.example.reportfrontapi.common.storage;

/**
 * presigned 업로드 발급 결과.
 *
 * @param objectKey S3 object key. 등록 API에 전달해 DB에 보관(바코드는 이 값을 저장).
 * @param uploadUrl presigned PUT URL. 클라이언트가 발급 때와 같은 Content-Type으로 직접 업로드.
 * @param publicUrl 공개 타입일 때의 영구 공개 URL(상품 썸네일용). 비공개 타입이면 null.
 */
public record PresignedUpload(
        String objectKey,
        String uploadUrl,
        String publicUrl
) {
}
