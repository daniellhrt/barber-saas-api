package br.com.daniel.danbarbersaasapi.domain.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.UUID;

public record OrderRequestDTO(
        @NotNull UUID clientId,
        @NotNull UUID barberId,
        @NotBlank(message = "O método de pagamento é obrigatório")
        @Pattern(regexp = "^(PIX|CARTAO|DINHEIRO)$", message = "O método de pagamento deve ser PIX, CARTAO ou DINHEIRO")
        String paymentMethod, // PIX, CARTAO, DINHEIRO
        String notes,

        @NotEmpty @Valid List<OrderItemRequestDTO> items
) {
}