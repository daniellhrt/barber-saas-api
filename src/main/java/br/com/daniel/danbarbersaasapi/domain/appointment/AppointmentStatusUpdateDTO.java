package br.com.daniel.danbarbersaasapi.domain.appointment;

import jakarta.validation.constraints.NotNull;

public record AppointmentStatusUpdateDTO(
        @NotNull(message = "O status é obrigatório")
        AppointmentStatus status
) {
}
