package br.com.daniel.danbarbersaasapi.domain.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductRequestDTO(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String name,

        @Size(max = 50, message = "A categoria deve ter no máximo 50 caracteres")
        String category,

        @Size(max = 50, message = "A marca deve ter no máximo 50 caracteres")
        String brand,

        @NotNull(message = "O preço é obrigatório")
        @Positive(message = "O preço deve ser maior que zero")
        BigDecimal price,

        @PositiveOrZero(message = "A quantidade em estoque não pode ser negativa")
        Integer stockQuantity,

        @Size(max = 50, message = "O SKU deve ter no máximo 50 caracteres")
        String sku
) {}