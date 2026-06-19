package com.example.reportfrontapi.domain.cost.application;

import java.math.BigInteger;
import java.time.LocalDate;

/**
 * 주별 소비 집계 응답(목록 요소). paymentAt 기준, ISO-8601(월요일 시작) 주차.
 * incomeAmount는 입금(INCREASE), expenseAmount는 출금(DECREASE) 건의 costAmount 합산이다(둘 다 양수).
 */
public record WeeklyCostResponse(
        int year,            // 주 기준 연도(ISO weekBasedYear)
        int week,            // ISO 주차
        LocalDate startDate, // 해당 주의 월요일
        BigInteger incomeAmount,   // 주별 입금금액 합산
        BigInteger expenseAmount   // 주별 출금금액 합산
) {
    public BigInteger getTotalAmount() {
        return incomeAmount.subtract(expenseAmount);
    }
}
