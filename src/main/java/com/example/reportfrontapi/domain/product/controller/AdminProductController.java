package com.example.reportfrontapi.domain.product.controller;

import com.example.reportfrontapi.common.response.ApiResponse;
import com.example.reportfrontapi.domain.product.application.ProductService;
import com.example.reportfrontapi.domain.product.application.dto.CodeLoadRequest;
import com.example.reportfrontapi.domain.product.application.dto.ProductCreateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 운영자 전용(ADMIN). SecurityConfig에서 /api/admin/** 은 ROLE_ADMIN 으로 제한.
 */
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Long> create(@Valid @RequestBody ProductCreateRequest request) {
        return ApiResponse.success(productService.create(request));
    }

    // 코드 재고 적재. 적재된 코드 수 반환.
    @PostMapping("/{id}/codes")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Integer> addCodes(@PathVariable Long id, @Valid @RequestBody CodeLoadRequest request) {
        return ApiResponse.success(productService.addCodes(id, request));
    }
}
