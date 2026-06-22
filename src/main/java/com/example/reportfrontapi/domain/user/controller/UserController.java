package com.example.reportfrontapi.domain.user.controller;

import com.example.reportfrontapi.common.response.ApiResponse;
import com.example.reportfrontapi.domain.user.application.UserPersonaService;
import com.example.reportfrontapi.domain.user.controller.dto.PersonaResponse;
import com.example.reportfrontapi.domain.user.controller.dto.PersonaUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserController {
    private final UserPersonaService userPersonaService;

    // 현재 사용자의 소비 페르소나 조회. null이면 첫 로그인 선택이 필요하다.
    @GetMapping("/persona")
    public ApiResponse<PersonaResponse> getPersona() {
        return ApiResponse.success(new PersonaResponse(userPersonaService.current()));
    }

    // 소비 페르소나 선택/변경. 배달러 선택 시 "배달 주문" 카테고리가 자동 생성된다.
    @PutMapping("/persona")
    public ApiResponse<PersonaResponse> updatePersona(@Valid @RequestBody PersonaUpdateRequest request) {
        return ApiResponse.success(new PersonaResponse(userPersonaService.change(request.persona())));
    }
}
