package com.example.reportfrontapi.common.storage.dto;

import com.example.reportfrontapi.common.storage.PresignedUpload;

public record UploadUrlResponse(
        String objectKey,
        String uploadUrl,
        String publicUrl
) {
    public static UploadUrlResponse from(PresignedUpload upload) {
        return new UploadUrlResponse(upload.objectKey(), upload.uploadUrl(), upload.publicUrl());
    }
}
