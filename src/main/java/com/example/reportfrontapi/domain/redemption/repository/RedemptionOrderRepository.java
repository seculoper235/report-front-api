package com.example.reportfrontapi.domain.redemption.repository;

import com.example.reportfrontapi.common.repository.BaseRepository;
import com.example.reportfrontapi.domain.redemption.model.QRedemptionOrder;
import com.example.reportfrontapi.domain.redemption.model.RedemptionOrder;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class RedemptionOrderRepository extends BaseRepository<RedemptionOrder, Long> {

    private static final QRedemptionOrder order = QRedemptionOrder.redemptionOrder;

    public RedemptionOrderRepository(EntityManager em) {
        super(RedemptionOrder.class, em);
    }

    // 멱등키로 기존 주문 조회(중복 차감 방지).
    public Optional<RedemptionOrder> findByIdempotencyKey(String idempotencyKey) {
        return Optional.ofNullable(
                selectFrom(order)
                        .where(order.idempotencyKey.eq(idempotencyKey))
                        .fetchOne());
    }

    // 사용자의 교환 내역(최신순).
    public List<RedemptionOrder> findByUserId(Long userId) {
        return selectFrom(order)
                .where(order.userId.eq(userId))
                .orderBy(order.createdAt.desc())
                .fetch();
    }
}
