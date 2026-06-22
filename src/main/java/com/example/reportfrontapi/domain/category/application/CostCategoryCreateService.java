package com.example.reportfrontapi.domain.category.application;

import com.example.reportfrontapi.domain.category.CategoryColor;
import com.example.reportfrontapi.domain.category.DuplicateCategoryException;
import com.example.reportfrontapi.domain.category.controller.dto.CostCategoryCreateRequest;
import com.example.reportfrontapi.domain.category.controller.dto.CostCategoryCreateResponse;
import com.example.reportfrontapi.domain.category.controller.dto.CostCategoryUpdateRequest;
import com.example.reportfrontapi.domain.category.model.CostCategory;
import com.example.reportfrontapi.domain.category.repository.CostCategoryRepository;
import com.example.reportfrontapi.web.security.SecurityUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CostCategoryCreateService {
    private final CostCategoryRepository costCategoryRepository;

    @Transactional
    public CostCategoryCreateResponse create(CostCategoryCreateRequest request) {
        Long userId = SecurityUtil.getRequiredCurrentUserId();
        // 같은 사용자 내 이름 중복 방지(다른 사용자와는 같은 이름 허용).
        if (costCategoryRepository.existsByNameAndOwner(request.categoryName(), userId)) {
            throw new DuplicateCategoryException(request.categoryName());
        }

        // 기존 색과 최대한 겹치지 않는 색을 부여한다.
        String color = CategoryColor.randomAvoiding(costCategoryRepository.findColorsByOwner(userId));
        CostCategory category = CostCategory.builder()
                .userId(userId)
                .categoryName(request.categoryName())
                .color(color)
                .build();

        return CostCategoryCreateResponse.from(costCategoryRepository.save(category));
    }

    @Transactional
    public CostCategoryCreateResponse update(Long id, CostCategoryUpdateRequest request) {
        Long userId = SecurityUtil.getRequiredCurrentUserId();
        CostCategory category = costCategoryRepository.findByIdAndOwner(id, userId)
                .orElseThrow(() -> new EntityNotFoundException("CostCategory not found: " + id));

        // 이름이 실제로 바뀔 때만 중복 검사(자기 자신과의 충돌은 무시).
        if (!category.getCategoryName().equals(request.categoryName())
                && costCategoryRepository.existsByNameAndOwner(request.categoryName(), userId)) {
            throw new DuplicateCategoryException(request.categoryName());
        }

        category.rename(request.categoryName());
        return CostCategoryCreateResponse.from(category);
    }
}
