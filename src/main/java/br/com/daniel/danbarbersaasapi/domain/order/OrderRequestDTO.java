package br.com.daniel.danbarbersaasapi.domain.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record OrderRequestDTO(
        @NotNull UUID clientId,
        @NotNull UUID barberId,
        String paymentMethod, // PIX, CARTAO, DINHEIRO
        String notes,

        @NotEmpty @Valid List<OrderItemRequestDTO> items
) {
}