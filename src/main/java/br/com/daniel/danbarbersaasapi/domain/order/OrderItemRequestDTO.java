package br.com.daniel.danbarbersaasapi.domain.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemRequestDTO(
        @NotNull UUID referenceId, // ID do Serviço (ex: Corte Degradê)
        @NotNull OrderItemType type, // SERVICE ou PRODUCT
        @NotNull @Positive Integer quantity,
        @NotNull @Positive BigDecimal unitPrice // Preço cobrado na hora (permite dar desconto)
) {
}