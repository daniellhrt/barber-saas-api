package br.com.daniel.danbarbersaasapi.domain.barber;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record BarberRequestDTO(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String name,

        @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres")
        String phone,

        @Size(max = 100, message = "A especialidade deve ter no máximo 100 caracteres")
        String specialty,

        @NotNull(message = "A comissão é obrigatória") BigDecimal commissionRate,

        // Dados para criar o acesso dele no sistema
        @NotBlank(message = "O e-mail de acesso é obrigatório")
        @Email(message = "O e-mail informado é inválido")
        String email,

        @NotBlank(message = "A senha de acesso é obrigatória")
        @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
        String password
) {
}