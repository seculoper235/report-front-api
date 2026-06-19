package com.example.reportfrontapi.common.storage.controller;

import com.example.reportfrontapi.common.response.ApiResponse;
import com.example.reportfrontapi.common.storage.PresignedUpload;
import com.example.reportfrontapi.common.storage.StorageService;
import com.example.reportfrontapi.common.storage.dto.UploadUrlRequest;
import com.example.reportfrontapi.common.storage.dto.UploadUrlResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 운영자 전용(ADMIN). SecurityConfig에서 /api/admin/** 은 ROLE_ADMIN 으로 제한.
 *
 * 이미지 업로드용 presigned URL을 발급한다. 운영자/클라는 반환된 uploadUrl로 S3에 직접 PUT 업로드한 뒤,
 * 상품 등록은 publicUrl을, 코드 적재는 objectKey를 각각 등록 API에 전달한다.
 */
@RestController
@RequestMapping("/api/admin/uploads")
@RequiredArgsConstructor
public class AdminUploadController {

    private final StorageService storageService;

    @PostMapping
    public ApiResponse<UploadUrlResponse> createUploadUrl(@Valid @RequestBody UploadUrlRequest request) {
        PresignedUpload upload = storageService.createUpload(request.type(), request.contentType());
        return ApiResponse.success(UploadUrlResponse.from(upload));
    }
}
