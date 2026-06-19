package com.example.reportfrontapi.domain.redemption.controller;

import com.example.reportfrontapi.common.response.ApiResponse;
import com.example.reportfrontapi.domain.redemption.application.RedemptionService;
import com.example.reportfrontapi.domain.redemption.application.dto.RedeemRequest;
import com.example.reportfrontapi.domain.redemption.application.dto.RedemptionResponse;
import com.example.reportfrontapi.web.security.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/redemptions")
@RequiredArgsConstructor
public class RedemptionController {

    private final RedemptionService redemptionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RedemptionResponse> redeem(@Valid @RequestBody RedeemRequest request) {
        Long userId = SecurityUtil.getRequiredCurrentUserId();
        return ApiResponse.success(
                redemptionService.redeem(userId, request.productId(), request.idempotencyKey()));
    }

    @GetMapping
    public ApiResponse<List<RedemptionResponse>> findHistory() {
        Long userId = SecurityUtil.getRequiredCurrentUserId();
        return ApiResponse.success(redemptionService.findHistory(userId));
    }
}
