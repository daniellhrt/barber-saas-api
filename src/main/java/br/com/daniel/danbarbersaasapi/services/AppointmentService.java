package br.com.daniel.danbarbersaasapi.services;

import br.com.daniel.danbarbersaasapi.domain.appointment.Appointment;
import br.com.daniel.danbarbersaasapi.domain.appointment.AppointmentRequestDTO;
import br.com.daniel.danbarbersaasapi.domain.appointment.AppointmentStatus;
import br.com.daniel.danbarbersaasapi.infra.exception.BusinessException;
import br.com.daniel.danbarbersaasapi.infra.exception.ResourceNotFoundException;
import br.com.daniel.danbarbersaasapi.repository.AppointmentRepository;
import br.com.daniel.danbarbersaasapi.repository.BarberRepository;
import br.com.daniel.danbarbersaasapi.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final BarberRepository barberRepository;
    private final ClientRepository clientRepository;

    @Transactional
    public Appointment create(AppointmentRequestDTO data) {
        var client = clientRepository.findById(data.clientId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado."));
        var barber = barberRepository.findById(data.barberId())
                .orElseThrow(() -> new ResourceNotFoundException("Barbeiro não encontrado."));

        int duration = data.durationMinutes() != null ? data.durationMinutes() : 30;
        OffsetDateTime endTime = data.scheduledTime().plusMinutes(duration);

        // Verifica conflito de horário para o barbeiro
        List<Appointment> conflicts = appointmentRepository.findConflicting(
                data.barberId(), data.scheduledTime(), endTime, null);
        if (!conflicts.isEmpty()) {
            throw new BusinessException(
                    "Conflito de horário: o barbeiro já tem um agendamento nesse período.");
        }

        Appointment appointment = new Appointment();
        appointment.setClient(client);
        appointment.setBarber(barber);
        appointment.setScheduledTime(data.scheduledTime());
        appointment.setDurationMinutes(duration);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setNotes(data.notes());
        appointment.setServiceId(data.serviceId());

        return appointmentRepository.save(appointment);
    }

    public Appointment findById(UUID id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado."));
    }

    /**
     * Busca agendamentos por período — usado para carregar o calendário.
     * Se barberId for informado, filtra por barbeiro específico.
     */
    public List<Appointment> findByPeriod(UUID barberId, OffsetDateTime startDate, OffsetDateTime endDate) {
        if (barberId != null) {
            return appointmentRepository.findByBarberIdAndPeriod(barberId, startDate, endDate);
        }
        return appointmentRepository.findByPeriod(startDate, endDate);
    }

    /**
     * Retorna a agenda do barbeiro para um dia específico.
     */
    public List<Appointment> findByBarberAndDate(UUID barberId, java.time.LocalDate date) {
        ZoneId zone = ZoneId.of("America/Sao_Paulo");
        OffsetDateTime startOfDay = date.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime endOfDay = date.atTime(java.time.LocalTime.MAX).atZone(zone).toOffsetDateTime();
        return appointmentRepository.findByBarberIdAndPeriod(barberId, startOfDay, endOfDay);
    }

    @Transactional
    public Appointment update(UUID id, AppointmentRequestDTO data) {
        Appointment appointment = findById(id);

        int duration = data.durationMinutes() != null ? data.durationMinutes() : appointment.getDurationMinutes();
        OffsetDateTime endTime = data.scheduledTime().plusMinutes(duration);

        // Verifica conflito, excluindo o próprio agendamento
        List<Appointment> conflicts = appointmentRepository.findConflicting(
                appointment.getBarber().getId(), data.scheduledTime(), endTime, id);
        if (!conflicts.isEmpty()) {
            throw new BusinessException(
                    "Conflito de horário: o barbeiro já tem um agendamento nesse período.");
        }

        appointment.setScheduledTime(data.scheduledTime());
        appointment.setDurationMinutes(duration);
        appointment.setNotes(data.notes());
        appointment.setServiceId(data.serviceId());

        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment updateStatus(UUID id, AppointmentStatus newStatus) {
        Appointment appointment = findById(id);

        // Valida transições de status permitidas
        validateStatusTransition(appointment.getStatus(), newStatus);
        appointment.setStatus(newStatus);

        return appointmentRepository.save(appointment);
    }

    @Transactional
    public void cancel(UUID id) {
        Appointment appointment = findById(id);
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BusinessException("Não é possível cancelar um agendamento já concluído.");
        }
        appointment.setStatus(AppointmentStatus.CANCELED);
        appointmentRepository.save(appointment);
    }

    private void validateStatusTransition(AppointmentStatus current, AppointmentStatus next) {
        boolean valid = switch (current) {
            case CONFIRMED -> next == AppointmentStatus.IN_PROGRESS || next == AppointmentStatus.CANCELED;
            case IN_PROGRESS -> next == AppointmentStatus.COMPLETED || next == AppointmentStatus.CANCELED;
            case COMPLETED, CANCELED -> false;
        };
        if (!valid) {
            throw new BusinessException(
                    "Transição de status inválida: " + current + " → " + next);
        }
    }
}
