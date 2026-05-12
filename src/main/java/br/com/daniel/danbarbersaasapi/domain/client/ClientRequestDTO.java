package br.com.daniel.danbarbersaasapi.domain.client;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.UUID;

public record ClientRequestDTO(
        @NotBlank(message = "O nome é obrigatório")
        String name,

        String phone,
        String whatsapp,
        String email,
        String cpf,
        LocalDate birthDate,
        String notes,
        String address,

        // ID opcional do barbeiro responsável (carteira)
        UUID primaryBarberId
) {
}