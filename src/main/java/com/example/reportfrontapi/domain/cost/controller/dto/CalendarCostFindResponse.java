package com.example.reportfrontapi.domain.cost.controller.dto;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;

/**
 * 캘린더(월) 소비 집계 응답. paymentAt 기준.
 * incomeAmount는 입금(INCREASE), expenseAmount는 출금(DECREASE) 건의 costAmount 합산이다(둘 다 양수).
 */
public record CalendarCostFindResponse(
        List<DailySummary> daily,    // 일별 정보
        MonthlySummary monthly       // 월별 정보
) {
    public record DailySummary(
            LocalDate date,          // 일자
            BigInteger incomeAmount, // 일별 입금금액 합산
            BigInteger expenseAmount // 일별 출금금액 합산
    ) {
    }

    public record MonthlySummary(
            int year,                // 년
            int month,               // 월
            BigInteger incomeAmount, // 월별 입금금액 합산
            BigInteger expenseAmount // 월별 출금금액 합산
    ) {
    }
}
