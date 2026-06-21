package com.example.reportfrontapi.domain.product.controller;

import com.example.reportfrontapi.common.response.ApiResponse;
import com.example.reportfrontapi.domain.product.application.ProductFindService;
import com.example.reportfrontapi.domain.product.controller.dto.ProductFindResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductFindService productFindService;

    // 기프티콘 상점 목록(상품별 재고 보유 여부 포함).
    @GetMapping
    public ApiResponse<List<ProductFindResponse>> findAll() {
        return ApiResponse.success(productFindService.findAll());
    }
}
