package com.example.reportfrontapi.common.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

/**
 * 이미지 저장(S3) 진입점.
 *
 * <ul>
 *   <li>업로드 = presigned PUT(클라이언트가 S3로 직접 업로드, 서버는 바이트를 거치지 않음).</li>
 *   <li>상품 썸네일(공개) = 공개 버킷 + 영구 공개 URL.</li>
 *   <li>바코드(민감) = 비공개 버킷 + 저장은 object key만, 조회 시 단기 presigned GET.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    private final S3Presigner presigner;
    private final S3Properties properties;

    /**
     * 업로드용 presigned URL 발급.
     * 반환된 uploadUrl로 PUT 업로드 시 같은 {@code contentType} 헤더를 보내야 서명이 일치한다.
     */
    public PresignedUpload createUpload(UploadType type, String contentType) {
        String bucket = bucketFor(type);
        String objectKey = type.getPrefix() + UUID.randomUUID() + extensionFor(contentType);

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(properties.getPresignExpirySeconds()))
                .putObjectRequest(objectRequest)
                .build();

        String uploadUrl = presigner.presignPutObject(presignRequest).url().toString();
        String publicUrl = type.isPublicAsset() ? buildPublicUrl(objectKey) : null;

        return new PresignedUpload(objectKey, uploadUrl, publicUrl);
    }

    /**
     * 비공개 바코드 컬럼 값을 노출용 URL로 변환.
     * null/blank → null, 이미 절대 URL(레거시) → 그대로, 그 외(object key) → 단기 presigned GET.
     * presign 실패는 응답 전체를 깨지 않도록 null로 폴백한다.
     */
    public String resolveBarcodeUrl(String stored) {
        if (stored == null || stored.isBlank()) {
            return null;
        }
        if (stored.startsWith("http://") || stored.startsWith("https://")) {
            return stored;
        }
        try {
            return presignGet(stored);
        } catch (RuntimeException e) {
            log.warn("바코드 presigned URL 발급 실패. 키 노출 방지를 위해 null 반환합니다.", e);
            return null;
        }
    }

    /** 비공개 버킷 object key에 대한 단기 presigned GET URL. */
    public String presignGet(String objectKey) {
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(properties.getPrivateBucket())
                .key(objectKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(properties.getPresignExpirySeconds()))
                .getObjectRequest(objectRequest)
                .build();

        return presigner.presignGetObject(presignRequest).url().toString();
    }

    private String bucketFor(UploadType type) {
        return type.isPublicAsset() ? properties.getPublicBucket() : properties.getPrivateBucket();
    }

    private String buildPublicUrl(String objectKey) {
        String base = properties.getPublicBaseUrl();
        if (base != null && !base.isBlank()) {
            return trimTrailingSlash(base) + "/" + objectKey;
        }
        return "https://" + properties.getPublicBucket()
                + ".s3." + properties.getRegion() + ".amazonaws.com/" + objectKey;
    }

    private String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String extensionFor(String contentType) {
        if (contentType == null) {
            return "";
        }
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> "";
        };
    }
}
