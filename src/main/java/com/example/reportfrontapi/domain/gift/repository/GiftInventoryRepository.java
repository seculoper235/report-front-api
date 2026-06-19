package com.example.reportfrontapi.domain.gift.repository;

import com.example.reportfrontapi.common.repository.BaseRepository;
import com.example.reportfrontapi.domain.gift.GiftInventory;
import com.example.reportfrontapi.domain.gift.GiftInventoryStatus;
import com.example.reportfrontapi.domain.gift.QGiftInventory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class GiftInventoryRepository extends BaseRepository<GiftInventory, Long> {

    private static final QGiftInventory inventory = QGiftInventory.giftInventory;

    public GiftInventoryRepository(EntityManager em) {
        super(GiftInventory.class, em);
    }

    // 해당 상품의 AVAILABLE 코드 1건을 비관적 쓰기 잠금으로 pop(동시 교환 시 중복 지급 방지).
    public Optional<GiftInventory> popAvailable(Long productId) {
        return Optional.ofNullable(
                selectFrom(inventory)
                        .where(inventory.productId.eq(productId),
                                inventory.status.eq(GiftInventoryStatus.AVAILABLE))
                        .orderBy(inventory.giftInventoryId.asc())
                        .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                        .fetchFirst());
    }

    // 해당 상품의 미사용(AVAILABLE) 코드 수(재고 보유 여부 판단).
    public long countAvailable(Long productId) {
        Long count = select(inventory.count())
                .from(inventory)
                .where(inventory.productId.eq(productId),
                        inventory.status.eq(GiftInventoryStatus.AVAILABLE))
                .fetchOne();
        return count != null ? count : 0L;
    }
}
