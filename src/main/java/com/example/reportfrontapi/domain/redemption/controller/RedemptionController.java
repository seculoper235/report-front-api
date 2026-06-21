package com.example.reportfrontapi.domain.redemption.controller;

import com.example.reportfrontapi.common.response.ApiResponse;
import com.example.reportfrontapi.domain.redemption.application.RedemptionCreateService;
import com.example.reportfrontapi.domain.redemption.application.RedemptionFindService;
import com.example.reportfrontapi.domain.redemption.controller.dto.RedeemCreateRequest;
import com.example.reportfrontapi.domain.redemption.controller.dto.RedemptionCreateResponse;
import com.example.reportfrontapi.domain.redemption.controller.dto.RedemptionFindResponse;
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

    private final RedemptionFindService redemptionFindService;
    private final RedemptionCreateService redemptionCreateService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RedemptionCreateResponse> redeem(@Valid @RequestBody RedeemCreateRequest request) {
        Long userId = SecurityUtil.getRequiredCurrentUserId();
        return ApiResponse.success(
                redemptionCreateService.redeem(userId, request.productId(), request.idempotencyKey()));
    }

    @GetMapping
    public ApiResponse<List<RedemptionFindResponse>> findHistory() {
        Long userId = SecurityUtil.getRequiredCurrentUserId();
        return ApiResponse.success(redemptionFindService.findHistory(userId));
    }
}
