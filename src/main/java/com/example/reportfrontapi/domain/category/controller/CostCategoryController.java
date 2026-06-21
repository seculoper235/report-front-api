package com.example.reportfrontapi.domain.category.controller;

import com.example.reportfrontapi.common.response.ApiResponse;
import com.example.reportfrontapi.domain.category.controller.dto.CostCategoryFindResponse;
import com.example.reportfrontapi.domain.category.application.CostCategoryFindService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cost-categories")
@RequiredArgsConstructor
public class CostCategoryController {
    private final CostCategoryFindService costCategoryFindService;

    // 현재 사용자의 지출 카테고리 목록. 등록/수정 화면의 카테고리 선택에 사용.
    @GetMapping
    public ApiResponse<List<CostCategoryFindResponse>> findAll() {
        return ApiResponse.success(costCategoryFindService.findAll());
    }
}
