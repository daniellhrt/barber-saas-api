package br.com.daniel.danbarbersaasapi.domain.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record ClientRequestDTO(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String name,

        @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres")
        String phone,

        @Size(max = 20, message = "O WhatsApp deve ter no máximo 20 caracteres")
        String whatsapp,

        @Email(message = "O e-mail informado é inválido")
        String email,

        @Size(max = 14, message = "O CPF deve ter no máximo 14 caracteres")
        String cpf,

        LocalDate birthDate,

        @Size(max = 2000, message = "As observações devem ter no máximo 2000 caracteres")
        String notes,

        @Size(max = 2000, message = "O endereço deve ter no máximo 2000 caracteres")
        String address,

        // ID opcional do barbeiro responsável (carteira)
        UUID primaryBarberId
) {
}