package com.example.reportfrontapi.domain.category.controller;

import com.example.reportfrontapi.common.response.ApiResponse;
import com.example.reportfrontapi.domain.category.application.CostCategoryCreateService;
import com.example.reportfrontapi.domain.category.application.CostCategoryDeleteService;
import com.example.reportfrontapi.domain.category.application.CostCategoryFindService;
import com.example.reportfrontapi.domain.category.controller.dto.CostCategoryCreateRequest;
import com.example.reportfrontapi.domain.category.controller.dto.CostCategoryCreateResponse;
import com.example.reportfrontapi.domain.category.controller.dto.CostCategoryFindResponse;
import com.example.reportfrontapi.domain.category.controller.dto.CostCategoryUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cost-categories")
@RequiredArgsConstructor
public class CostCategoryController {
    private final CostCategoryFindService costCategoryFindService;
    private final CostCategoryCreateService costCategoryCreateService;
    private final CostCategoryDeleteService costCategoryDeleteService;

    // 현재 사용자의 지출 카테고리 목록. 등록/수정 화면의 카테고리 선택에 사용.
    @GetMapping
    public ApiResponse<List<CostCategoryFindResponse>> findAll() {
        return ApiResponse.success(costCategoryFindService.findAll());
    }

    // 현재 사용자의 지출 카테고리 추가.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CostCategoryCreateResponse> create(@Valid @RequestBody CostCategoryCreateRequest request) {
        return ApiResponse.success(costCategoryCreateService.create(request));
    }

    // 카테고리 이름 수정.
    @PutMapping("/{id}")
    public ApiResponse<CostCategoryCreateResponse> update(@PathVariable Long id,
                                                          @Valid @RequestBody CostCategoryUpdateRequest request) {
        return ApiResponse.success(costCategoryCreateService.update(id, request));
    }

    // 카테고리 삭제. 연결된 소비 내역이 있으면 409.
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        costCategoryDeleteService.delete(id);
        return ApiResponse.success(null);
    }
}
