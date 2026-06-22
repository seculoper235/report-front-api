package com.example.reportfrontapi.domain.user.controller.dto;

import com.example.reportfrontapi.domain.user.model.CostPersona;

public record PersonaResponse(
        CostPersona persona   // null이면 아직 미선택(첫 로그인 선택 필요)
) {
}
