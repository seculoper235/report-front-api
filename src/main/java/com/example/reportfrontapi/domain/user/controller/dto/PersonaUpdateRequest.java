package com.example.reportfrontapi.domain.user.controller.dto;

import com.example.reportfrontapi.domain.user.model.CostPersona;
import jakarta.validation.constraints.NotNull;

public record PersonaUpdateRequest(
        @NotNull
        CostPersona persona
) {
}
