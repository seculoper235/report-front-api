package com.example.reportfrontapi.domain.cost.repository;

import com.example.reportfrontapi.common.repository.BaseRepository;
import com.example.reportfrontapi.domain.cost.model.CostCategory;
import com.example.reportfrontapi.domain.cost.model.QCostCategory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

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
}
