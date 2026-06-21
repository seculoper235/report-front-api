package com.example.reportfrontapi.domain.product.repository;

import com.example.reportfrontapi.common.repository.BaseRepository;
import com.example.reportfrontapi.domain.product.model.Product;
import com.example.reportfrontapi.domain.product.model.QProduct;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductRepository extends BaseRepository<Product, Long> {

    private static final QProduct product = QProduct.product;

    public ProductRepository(EntityManager em) {
        super(Product.class, em);
    }

    // 노출 중(active)인 상품 단건 조회.
    public Optional<Product> findActiveById(Long id) {
        return Optional.ofNullable(
                selectFrom(product)
                        .where(product.productId.eq(id), product.active.isTrue())
                        .fetchOne());
    }

    // 노출 중인 전체 상품.
    public List<Product> findAllActive() {
        return selectFrom(product)
                .where(product.active.isTrue())
                .orderBy(product.productId.asc())
                .fetch();
    }
}
