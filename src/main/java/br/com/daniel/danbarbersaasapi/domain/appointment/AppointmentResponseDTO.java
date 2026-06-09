package br.com.daniel.danbarbersaasapi.domain.appointment;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AppointmentResponseDTO(
        UUID id,
        UUID clientId,
        String clientName,
        String clientPhone,
        String clientWhatsappLink,
        UUID barberId,
        String barberName,
        OffsetDateTime scheduledTime,
        OffsetDateTime endTime,
        Integer durationMinutes,
        AppointmentStatus status,
        String notes,
        UUID serviceId,
        OffsetDateTime createdAt
) {
    public AppointmentResponseDTO(Appointment appointment) {
        this(
                appointment.getId(),
                appointment.getClient().getId(),
                appointment.getClient().getName(),
                appointment.getClient().getPhone(),
                buildWhatsappLink(appointment.getClient().getWhatsapp()),
                appointment.getBarber().getId(),
                appointment.getBarber().getName(),
                appointment.getScheduledTime(),
                appointment.getEndTime(),
                appointment.getDurationMinutes(),
                appointment.getStatus(),
                appointment.getNotes(),
                appointment.getServiceId(),
                appointment.getCreatedAt()
        );
    }

    private static String buildWhatsappLink(String whatsapp) {
        if (whatsapp == null || whatsapp.isBlank()) return null;
        String digits = whatsapp.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return null;
        String number = digits.startsWith("55") ? digits : "55" + digits;
        return "https://wa.me/" + number;
    }
}
