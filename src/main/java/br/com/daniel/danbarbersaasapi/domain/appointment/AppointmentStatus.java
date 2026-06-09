package br.com.daniel.danbarbersaasapi.domain.appointment;

/**
 * Status do agendamento com transições válidas:
 *
 * CONFIRMED → IN_PROGRESS → COMPLETED
 * CONFIRMED → CANCELED
 * IN_PROGRESS → CANCELED
 */
public enum AppointmentStatus {
    CONFIRMED,
    IN_PROGRESS,
    COMPLETED,
    CANCELED
}