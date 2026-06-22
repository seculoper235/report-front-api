package com.example.reportfrontapi.domain.user.application;

import com.example.reportfrontapi.domain.category.CategoryColor;
import com.example.reportfrontapi.domain.category.model.CostCategory;
import com.example.reportfrontapi.domain.category.repository.CostCategoryRepository;
import com.example.reportfrontapi.domain.user.model.CostPersona;
import com.example.reportfrontapi.domain.user.model.User;
import com.example.reportfrontapi.domain.user.repository.UserRepository;
import com.example.reportfrontapi.web.security.SecurityUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 소비 페르소나 조회/변경. 배달러 선택 시 "배달 주문" 카테고리를 자동 생성한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserPersonaService {
    // 배달러의 배달 주문 집계 대상 카테고리. 이 카테고리의 BAD 소비가 배달 주문으로 집계된다.
    public static final String DELIVERY_CATEGORY_NAME = "배달 주문";

    private final UserRepository userRepository;
    private final CostCategoryRepository costCategoryRepository;

    public CostPersona current() {
        return loadCurrentUser().getCostPersona();
    }

    @Transactional
    public CostPersona change(CostPersona persona) {
        User user = loadCurrentUser();
        user.changePersona(persona);
        if (persona == CostPersona.DELIVERY) {
            ensureDeliveryCategory(user.getUserId());
        }
        return persona;
    }

    // 배달 주문 카테고리가 없으면 생성한다(이미 있으면 그대로 둔다).
    private void ensureDeliveryCategory(Long userId) {
        if (costCategoryRepository.existsByNameAndOwner(DELIVERY_CATEGORY_NAME, userId)) {
            return;
        }
        String color = CategoryColor.randomAvoiding(costCategoryRepository.findColorsByOwner(userId));
        costCategoryRepository.save(CostCategory.builder()
                .userId(userId)
                .categoryName(DELIVERY_CATEGORY_NAME)
                .color(color)
                .build());
    }

    private User loadCurrentUser() {
        Long userId = SecurityUtil.getRequiredCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
    }
}
