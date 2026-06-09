package br.com.daniel.danbarbersaasapi.domain.appointment;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AppointmentRequestDTO(
        @NotNull(message = "O cliente é obrigatório")
        UUID clientId,

        @NotNull(message = "O barbeiro é obrigatório")
        UUID barberId,

        @NotNull(message = "O horário do agendamento é obrigatório")
        @Future(message = "O agendamento deve ser em uma data futura")
        OffsetDateTime scheduledTime,

        /**
         * Duração em minutos. Se não informado, usa 30 min como padrão.
         * Valores permitidos: 10, 15, 20, 30, 45, 60, 90, 120
         */
        @Positive(message = "A duração deve ser positiva")
        Integer durationMinutes,

        /** ID do serviço agendado (opcional) */
        UUID serviceId,

        String notes
) {
}
