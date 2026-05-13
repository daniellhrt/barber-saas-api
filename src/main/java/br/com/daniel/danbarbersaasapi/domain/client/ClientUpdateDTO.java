package br.com.daniel.danbarbersaasapi.domain.client;

import java.time.LocalDate;

public record ClientUpdateDTO(
        String name,
        String phone,
        String email,
        String cpf,
        LocalDate birthDate,
        String notes,
        String address
) {
}