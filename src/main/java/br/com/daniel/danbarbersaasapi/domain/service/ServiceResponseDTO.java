package br.com.daniel.danbarbersaasapi.domain.service;

import java.math.BigDecimal;
import java.util.UUID;

public record ServiceResponseDTO(
        UUID id,
        String name,
        BigDecimal price,
        Integer estimatedDurationMinutes,
        String description
) {
    // Construtor para transformar facilmente a Entidade no DTO de resposta
    public ServiceResponseDTO(BarberService service) {
        this(
                service.getId(),
                service.getName(),
                service.getPrice(),
                service.getEstimatedDurationMinutes(),
                service.getDescription()
        );
    }
}