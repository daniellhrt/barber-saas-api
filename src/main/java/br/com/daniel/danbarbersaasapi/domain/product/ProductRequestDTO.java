package br.com.daniel.danbarbersaasapi.domain.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ProductRequestDTO(
        @NotBlank(message = "O nome é obrigatório") String name,
        String category,
        String brand,
        @NotNull(message = "O preço é obrigatório") BigDecimal price,
        Integer stockQuantity,
        String sku
) {}