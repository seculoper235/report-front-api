package com.example.reportfrontapi.domain.category.application;

import com.example.reportfrontapi.domain.category.CategoryInUseException;
import com.example.reportfrontapi.domain.category.model.CostCategory;
import com.example.reportfrontapi.domain.category.repository.CostCategoryRepository;
import com.example.reportfrontapi.domain.cost.repository.ReportCostRepository;
import com.example.reportfrontapi.web.security.SecurityUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CostCategoryDeleteService {
    private final CostCategoryRepository costCategoryRepository;
    private final ReportCostRepository reportCostRepository;

    @Transactional
    public void delete(Long id) {
        Long userId = SecurityUtil.getRequiredCurrentUserId();
        CostCategory category = costCategoryRepository.findByIdAndOwner(id, userId)
                .orElseThrow(() -> new EntityNotFoundException("CostCategory not found: " + id));

        // 소비 내역의 카테고리(FK)는 NOT NULL이므로, 연결된 소비가 있으면 삭제를 막는다.
        if (reportCostRepository.existsByCategoryId(id, userId)) {
            throw new CategoryInUseException(id);
        }

        costCategoryRepository.delete(category);
    }
}
