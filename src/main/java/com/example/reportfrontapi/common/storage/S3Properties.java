package com.example.reportfrontapi.common.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * S3 이미지 저장 설정. (prefix: aws.s3)
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "aws.s3")
public class S3Properties {

    /** AWS 리전. */
    private String region = "ap-northeast-2";

    /** 상품 썸네일(공개) 버킷. */
    private String publicBucket;

    /** 바코드 이미지(비공개) 버킷. */
    private String privateBucket;

    /** 공개 버킷의 CDN/커스텀 도메인 base URL. 비어 있으면 S3 가상호스팅 URL 사용. */
    private String publicBaseUrl;

    /** presigned URL(업로드/조회) 만료 시간(초). */
    private long presignExpirySeconds = 600;
}
