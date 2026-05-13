package br.com.daniel.danbarbersaasapi.domain.barber;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BarberRequestDTO(
        @NotBlank(message = "O nome é obrigatório") String name,
        String phone,
        String specialty,
        @NotNull(message = "A comissão é obrigatória") BigDecimal commissionRate,

        // Dados para criar o acesso dele no sistema
        @NotBlank(message = "O e-mail de acesso é obrigatório") String email,
        @NotBlank(message = "A senha de acesso é obrigatória") String password
) {
}