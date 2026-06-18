package com.example.reportfrontapi.domain.cost.repository;

import com.example.reportfrontapi.domain.cost.ReportCost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportCostRepository extends JpaRepository<ReportCost, Long> {

    // paymentAt이 [start, end) 범위에 드는 소비 조회.
    List<ReportCost> findByPaymentAtGreaterThanEqualAndPaymentAtLessThan(LocalDateTime start, LocalDateTime end);

    // 카테고리 이름이 일치하는 소비 조회.
    List<ReportCost> findByCategoryName(String categoryName);
}
