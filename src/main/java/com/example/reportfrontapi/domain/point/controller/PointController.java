package com.example.reportfrontapi.domain.point.controller;

import com.example.reportfrontapi.common.response.ApiResponse;
import com.example.reportfrontapi.domain.point.application.PointBalanceResponse;
import com.example.reportfrontapi.domain.point.application.PointLedgerResponse;
import com.example.reportfrontapi.domain.point.application.PointService;
import com.example.reportfrontapi.web.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    // 교환 차감이 반영된 현재 포인트 잔액.
    @GetMapping("/balance")
    public ApiResponse<PointBalanceResponse> getBalance() {
        int balance = pointService.getBalance(SecurityUtil.getRequiredCurrentUserId());
        return ApiResponse.success(new PointBalanceResponse(balance));
    }

    // 적립/차감/조정 원장 내역(최신순).
    @GetMapping("/ledger")
    public ApiResponse<List<PointLedgerResponse>> getLedger() {
        return ApiResponse.success(pointService.getLedger(SecurityUtil.getRequiredCurrentUserId()));
    }
}
