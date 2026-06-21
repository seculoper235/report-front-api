package com.example.reportfrontapi.domain.redemption.application;

import com.example.reportfrontapi.common.storage.StorageService;
import com.example.reportfrontapi.domain.gift.model.GiftInventory;
import com.example.reportfrontapi.domain.gift.model.GiftInventoryStatus;
import com.example.reportfrontapi.domain.gift.repository.GiftInventoryRepository;
import com.example.reportfrontapi.domain.point.application.PointCreateService;
import com.example.reportfrontapi.domain.point.application.PointFindService;
import com.example.reportfrontapi.domain.product.Product;
import com.example.reportfrontapi.domain.product.repository.ProductRepository;
import com.example.reportfrontapi.domain.redemption.InsufficientPointException;
import com.example.reportfrontapi.domain.redemption.OutOfStockException;
import com.example.reportfrontapi.domain.redemption.RedemptionOrder;
import com.example.reportfrontapi.domain.redemption.RedemptionStatus;
import com.example.reportfrontapi.domain.redemption.application.dto.RedemptionResponse;
import com.example.reportfrontapi.domain.redemption.repository.RedemptionOrderRepository;
import com.example.reportfrontapi.domain.user.model.Role;
import com.example.reportfrontapi.domain.user.model.User;
import com.example.reportfrontapi.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RedemptionServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private GiftInventoryRepository giftInventoryRepository;
    @Mock private RedemptionOrderRepository redemptionOrderRepository;
    @Mock private PointFindService pointFindService;
    @Mock private PointCreateService pointCreateService;
    @Mock private UserRepository userRepository;
    @Mock private StorageService storageService;

    @InjectMocks
    private RedemptionService redemptionService;

    private Product product(int pointCost) {
        return Product.builder().productId(10L).name("스타벅스").pointCost(pointCost).active(true).build();
    }

    private RedemptionOrder savedOrder() {
        return RedemptionOrder.builder()
                .redemptionOrderId(5L).userId(1L).productId(10L).pointCost(100)
                .status(RedemptionStatus.ISSUED).idempotencyKey("idem-1").giftInventoryId(99L)
                .build();
    }

    @Test
    @DisplayName("교환 성공: 잔액 충분 + 재고 있으면 코드 지급 주문을 생성한다")
    void redeem_success() {
        given(redemptionOrderRepository.findByIdempotencyKey("idem-1")).willReturn(Optional.empty());
        given(userRepository.findByIdForUpdate(1L)).willReturn(Optional.of(
                User.builder().userId(1L).email("u@x.com").password("p").role(Role.USER).build()));
        given(productRepository.findActiveById(10L)).willReturn(Optional.of(product(100)));
        given(pointFindService.getBalance(1L)).willReturn(150);
        given(giftInventoryRepository.popAvailable(10L)).willReturn(Optional.of(
                GiftInventory.of(10L, "GIFT-CODE-XYZ", null, null)));
        given(redemptionOrderRepository.save(ArgumentMatchers.any(RedemptionOrder.class)))
                .willReturn(savedOrder());

        RedemptionResponse response = redemptionService.redeem(1L, 10L, "idem-1");

        assertThat(response.orderId()).isEqualTo(5L);
        assertThat(response.pointCost()).isEqualTo(100);
        assertThat(response.status()).isEqualTo("ISSUED");
        assertThat(response.code()).isEqualTo("GIFT-CODE-XYZ");
        verify(redemptionOrderRepository).save(ArgumentMatchers.any(RedemptionOrder.class));
        verify(pointCreateService).recordRedeem(1L, 100, 5L);
    }

    @Test
    @DisplayName("멱등: 같은 키로 재요청하면 기존 주문을 반환하고 재차감하지 않는다")
    void redeem_idempotent() {
        given(redemptionOrderRepository.findByIdempotencyKey("idem-1")).willReturn(Optional.of(savedOrder()));
        given(productRepository.findById(10L)).willReturn(Optional.of(product(100)));
        given(giftInventoryRepository.findById(99L)).willReturn(Optional.of(
                GiftInventory.of(10L, "GIFT-CODE-XYZ", null, null)));

        RedemptionResponse response = redemptionService.redeem(1L, 10L, "idem-1");

        assertThat(response.orderId()).isEqualTo(5L);
        verify(userRepository, never()).findByIdForUpdate(ArgumentMatchers.anyLong());
        verify(redemptionOrderRepository, never()).save(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("잔액 부족: 차감/재고 pop 없이 InsufficientPointException")
    void redeem_insufficientPoint() {
        given(redemptionOrderRepository.findByIdempotencyKey("idem-1")).willReturn(Optional.empty());
        given(userRepository.findByIdForUpdate(1L)).willReturn(Optional.of(
                User.builder().userId(1L).email("u@x.com").password("p").role(Role.USER).build()));
        given(productRepository.findActiveById(10L)).willReturn(Optional.of(product(100)));
        given(pointFindService.getBalance(1L)).willReturn(50);

        assertThatThrownBy(() -> redemptionService.redeem(1L, 10L, "idem-1"))
                .isInstanceOf(InsufficientPointException.class);

        verify(giftInventoryRepository, never()).popAvailable(ArgumentMatchers.anyLong());
        verify(redemptionOrderRepository, never()).save(ArgumentMatchers.any());
        verify(pointCreateService, never()).recordRedeem(ArgumentMatchers.anyLong(),
                ArgumentMatchers.anyInt(), ArgumentMatchers.anyLong());
    }

    @Test
    @DisplayName("재고 없음: 차감 없이 OutOfStockException")
    void redeem_outOfStock() {
        given(redemptionOrderRepository.findByIdempotencyKey("idem-1")).willReturn(Optional.empty());
        given(userRepository.findByIdForUpdate(1L)).willReturn(Optional.of(
                User.builder().userId(1L).email("u@x.com").password("p").role(Role.USER).build()));
        given(productRepository.findActiveById(10L)).willReturn(Optional.of(product(100)));
        given(pointFindService.getBalance(1L)).willReturn(150);
        given(giftInventoryRepository.popAvailable(10L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> redemptionService.redeem(1L, 10L, "idem-1"))
                .isInstanceOf(OutOfStockException.class);

        verify(redemptionOrderRepository, never()).save(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("교환 성공 시 지급한 재고 코드가 ISSUED로 변경된다")
    void redeem_marksInventoryIssued() {
        GiftInventory inventory = GiftInventory.of(10L, "GIFT-CODE-XYZ", null, null);
        given(redemptionOrderRepository.findByIdempotencyKey("idem-1")).willReturn(Optional.empty());
        given(userRepository.findByIdForUpdate(1L)).willReturn(Optional.of(
                User.builder().userId(1L).email("u@x.com").password("p").role(Role.USER).build()));
        given(productRepository.findActiveById(10L)).willReturn(Optional.of(product(100)));
        given(pointFindService.getBalance(1L)).willReturn(150);
        given(giftInventoryRepository.popAvailable(10L)).willReturn(Optional.of(inventory));
        given(redemptionOrderRepository.save(ArgumentMatchers.any(RedemptionOrder.class)))
                .willReturn(savedOrder());

        redemptionService.redeem(1L, 10L, "idem-1");

        assertThat(inventory.getStatus()).isEqualTo(GiftInventoryStatus.ISSUED);
        assertThat(inventory.getRedemptionOrderId()).isEqualTo(5L);
    }
}
