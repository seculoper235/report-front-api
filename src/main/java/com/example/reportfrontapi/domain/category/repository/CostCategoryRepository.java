package com.example.reportfrontapi.domain.category.repository;

import com.example.reportfrontapi.common.repository.BaseRepository;
import com.example.reportfrontapi.domain.category.controller.dto.CostCategoryFindResponse;
import com.example.reportfrontapi.domain.category.model.CostCategory;
import com.example.reportfrontapi.domain.category.model.QCostCategory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CostCategoryRepository extends BaseRepository<CostCategory, Long> {

    private static final QCostCategory category = QCostCategory.costCategory;

    public CostCategoryRepository(EntityManager em) {
        super(CostCategory.class, em);
    }

    // 소유자(user_id) + id로 단건 조회. 타 사용자 카테고리는 조회되지 않는다.
    public Optional<CostCategory> findByIdAndOwner(Long id, Long userId) {
        return Optional.ofNullable(
                selectFrom(category)
                        .where(category.categoryId.eq(id), category.userId.eq(userId))
                        .fetchOne());
    }

    // 소유자(user_id)의 카테고리 목록을 id 오름차순으로 조회해 응답 DTO로 반환.
    public List<CostCategoryFindResponse> findAllByOwner(Long userId) {
        return select(CostCategoryFindResponse.class,
                category.categoryId, category.categoryName, category.color)
                .from(category)
                .where(category.userId.eq(userId))
                .orderBy(category.categoryId.asc())
                .fetch();
    }

    // 소유자(user_id)가 같은 이름의 카테고리를 이미 가지고 있는지 여부.
    public boolean existsByNameAndOwner(String categoryName, Long userId) {
        return selectFrom(category)
                .where(category.userId.eq(userId), category.categoryName.eq(categoryName))
                .fetchFirst() != null;
    }

    // 소유자(user_id)의 이름이 일치하는 카테고리 ID(배달 주문 카테고리 조회용). 없으면 empty.
    public Optional<Long> findIdByNameAndOwner(String categoryName, Long userId) {
        return Optional.ofNullable(
                select(category.categoryId)
                        .from(category)
                        .where(category.userId.eq(userId), category.categoryName.eq(categoryName))
                        .fetchFirst());
    }

    // 소유자(user_id)가 사용 중인 색상 목록(중복 회피용).
    public List<String> findColorsByOwner(Long userId) {
        return select(category.color)
                .from(category)
                .where(category.userId.eq(userId))
                .fetch();
    }
}
