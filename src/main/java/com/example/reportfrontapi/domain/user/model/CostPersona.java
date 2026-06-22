package com.example.reportfrontapi.domain.user.model;

/**
 * 소비 페르소나. 첫 로그인 시 선택하며 이후 설정에서 변경할 수 있다.
 * 나쁜 소비(BAD) 차감 로직이 페르소나별로 달라진다.
 */
public enum CostPersona {
    DELIVERY,       // 배달러 — 금액 곡선 + 최근 3일 배달 빈도
    MICRO_SPENDER,  // 소과금러 — 금액 곡선 + 당일 BAD 개수
    JACKPOT         // 한탕주의 — 미구현(빈 스텁)
}
