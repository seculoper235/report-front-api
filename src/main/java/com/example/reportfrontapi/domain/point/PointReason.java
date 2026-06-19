package com.example.reportfrontapi.domain.point;

public enum PointReason {
    EARN_COST,  // 소비 등록 적립
    REDEEM,     // 기프티콘 교환 차감
    ADJUST,     // 소비 수정/삭제 등 조정
    REFUND      // 교환 취소 환불
}
