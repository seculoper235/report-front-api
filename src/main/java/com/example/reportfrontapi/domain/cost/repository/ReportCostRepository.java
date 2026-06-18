package com.example.reportfrontapi.domain.cost.repository;

import com.example.reportfrontapi.domain.cost.ReportCost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportCostRepository extends JpaRepository<ReportCost, Long> {
}
