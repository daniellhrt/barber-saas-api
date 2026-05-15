package br.com.daniel.danbarbersaasapi.domain.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ServiceRequestDTO(
        @NotBlank(message = "O nome do serviço é obrigatório")
        String name,

        @NotNull(message = "O preço é obrigatório")
        @Positive(message = "O preço deve ser maior que zero")
        BigDecimal price,

        @NotNull(message = "A duração estimada é obrigatória")
        @Positive(message = "A duração deve ser maior que zero")
        Integer estimatedDurationMinutes,

        @Size(max = 2000, message = "A descrição deve ter no máximo 2000 caracteres")
        String description
) {
}