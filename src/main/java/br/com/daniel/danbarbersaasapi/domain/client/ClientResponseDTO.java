package br.com.daniel.danbarbersaasapi.domain.client;

import java.time.LocalDate;
import java.util.UUID;

public record ClientResponseDTO(
        UUID id,
        String name,
        String phone,
        String whatsapp,
        String whatsappLink,
        String email,
        String cpf,
        LocalDate birthDate,
        String notes,
        String address,
        Integer returnIntervalDays,
        UUID primaryBarberId,
        String primaryBarberName,
        Integer daysSinceLastVisit
) {
    /** Construtor padrão — sem informação de dias desde última visita */
    public ClientResponseDTO(Client client) {
        this(
                client.getId(),
                client.getName(),
                client.getPhone(),
                client.getWhatsapp(),
                buildWhatsappLink(client.getWhatsapp()),
                client.getEmail(),
                client.getCpf(),
                client.getBirthDate(),
                client.getNotes(),
                client.getAddress(),
                client.getReturnIntervalDays(),
                client.getPrimaryBarber() != null ? client.getPrimaryBarber().getId() : null,
                client.getPrimaryBarber() != null ? client.getPrimaryBarber().getName() : null,
                null
        );
    }

    /** Construtor para listagem de clientes overdue — inclui dias desde última visita */
    public ClientResponseDTO(Client client, Integer daysSinceLastVisit) {
        this(
                client.getId(),
                client.getName(),
                client.getPhone(),
                client.getWhatsapp(),
                buildWhatsappLink(client.getWhatsapp()),
                client.getEmail(),
                client.getCpf(),
                client.getBirthDate(),
                client.getNotes(),
                client.getAddress(),
                client.getReturnIntervalDays(),
                client.getPrimaryBarber() != null ? client.getPrimaryBarber().getId() : null,
                client.getPrimaryBarber() != null ? client.getPrimaryBarber().getName() : null,
                daysSinceLastVisit
        );
    }

    /**
     * Gera link de WhatsApp no formato https://wa.me/55{numero}
     * Remove todos os caracteres não numéricos do número
     */
    private static String buildWhatsappLink(String whatsapp) {
        if (whatsapp == null || whatsapp.isBlank()) return null;
        String digits = whatsapp.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return null;
        // Se já tem DDI, usa direto. Se não, prefere DDI Brasil 55
        String number = digits.startsWith("55") ? digits : "55" + digits;
        return "https://wa.me/" + number;
    }
}