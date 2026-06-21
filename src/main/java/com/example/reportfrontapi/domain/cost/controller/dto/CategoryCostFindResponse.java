package com.example.reportfrontapi.domain.cost.controller.dto;

import java.math.BigInteger;

/**
 * category별 소비 집계 응답(목록 요소). paymentAt 기준.
 * incomeAmount는 입금(INCREASE), expenseAmount는 출금(DECREASE) 건의 costAmount 합산이다(둘 다 양수).
 */
public record CategoryCostFindResponse(
        Long categoryId,       // 카테고리 ID
        String categoryName,   // 카테고리 이름
        BigInteger incomeAmount,   // category별 입금금액 합산
        BigInteger expenseAmount   // category별 출금금액 합산
) {
}
