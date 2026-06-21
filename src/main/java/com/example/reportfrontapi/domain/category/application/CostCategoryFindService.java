package com.example.reportfrontapi.domain.category.application;

import com.example.reportfrontapi.domain.category.controller.dto.CostCategoryFindResponse;
import com.example.reportfrontapi.domain.category.repository.CostCategoryRepository;
import com.example.reportfrontapi.web.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CostCategoryFindService {
    private final CostCategoryRepository costCategoryRepository;

    // 현재 사용자의 지출 카테고리 목록 조회.
    public List<CostCategoryFindResponse> findAll() {
        return costCategoryRepository.findAllByOwner(SecurityUtil.getRequiredCurrentUserId());
    }
}
