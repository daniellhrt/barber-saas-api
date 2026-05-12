package br.com.daniel.danbarbersaasapi.domain.client;

import java.time.LocalDate;
import java.util.UUID;

public record ClientResponseDTO(
        UUID id,
        String name,
        String phone,
        String whatsapp,
        String email,
        String cpf,
        LocalDate birthDate,
        String notes,
        String address,
        UUID primaryBarberId,
        String primaryBarberName
) {
    public ClientResponseDTO(Client client) {
        this(
                client.getId(),
                client.getName(),
                client.getPhone(),
                client.getWhatsapp(),
                client.getEmail(),
                client.getCpf(),
                client.getBirthDate(),
                client.getNotes(),
                client.getAddress(),
                client.getPrimaryBarber() != null ? client.getPrimaryBarber().getId() : null,
                client.getPrimaryBarber() != null ? client.getPrimaryBarber().getName() : null
        );
    }
}