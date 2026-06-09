package br.com.daniel.danbarbersaasapi.services;

import br.com.daniel.danbarbersaasapi.domain.appointment.Appointment;
import br.com.daniel.danbarbersaasapi.domain.appointment.AppointmentRequestDTO;
import br.com.daniel.danbarbersaasapi.domain.appointment.AppointmentStatus;
import br.com.daniel.danbarbersaasapi.domain.barber.Barber;
import br.com.daniel.danbarbersaasapi.domain.client.Client;
import br.com.daniel.danbarbersaasapi.infra.exception.BusinessException;
import br.com.daniel.danbarbersaasapi.infra.exception.ResourceNotFoundException;
import br.com.daniel.danbarbersaasapi.repository.AppointmentRepository;
import br.com.daniel.danbarbersaasapi.repository.BarberRepository;
import br.com.daniel.danbarbersaasapi.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private BarberRepository barberRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private UUID clientId;
    private UUID barberId;
    private UUID appointmentId;
    private Client client;
    private Barber barber;
    private OffsetDateTime scheduledTime;

    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();
        barberId = UUID.randomUUID();
        appointmentId = UUID.randomUUID();

        client = new Client();
        client.setId(clientId);
        client.setName("João");

        barber = new Barber();
        barber.setId(barberId);
        barber.setName("Carlos");

        scheduledTime = OffsetDateTime.of(2026, 6, 15, 10, 0, 0, 0, ZoneOffset.of("-03:00"));
    }

    @Test
    @DisplayName("Deve criar agendamento sem conflito de horário")
    void shouldCreateAppointmentWithoutConflict() {
        var request = new AppointmentRequestDTO(clientId, barberId, scheduledTime, 30, null, null);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(barberRepository.findById(barberId)).thenReturn(Optional.of(barber));
        when(appointmentRepository.findConflicting(eq(barberId), any(), any(), eq(null)))
                .thenReturn(Collections.emptyList());
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> {
            Appointment a = inv.getArgument(0);
            a.setId(appointmentId);
            return a;
        });

        Appointment result = appointmentService.create(request);

        assertThat(result.getId()).isEqualTo(appointmentId);
        assertThat(result.getClient().getName()).isEqualTo("João");
        assertThat(result.getDurationMinutes()).isEqualTo(30);
        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Deve lançar exceção para conflito de horário")
    void shouldThrowForTimeConflict() {
        var request = new AppointmentRequestDTO(clientId, barberId, scheduledTime, 30, null, null);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(barberRepository.findById(barberId)).thenReturn(Optional.of(barber));

        Appointment conflicting = new Appointment();
        conflicting.setId(UUID.randomUUID());
        when(appointmentRepository.findConflicting(eq(barberId), any(), any(), eq(null)))
                .thenReturn(List.of(conflicting));

        assertThatThrownBy(() -> appointmentService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Conflito de horário");
    }

    @Test
    @DisplayName("Deve usar 30 minutos como duração padrão")
    void shouldUseDefaultDuration() {
        var request = new AppointmentRequestDTO(clientId, barberId, scheduledTime, null, null, null);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(barberRepository.findById(barberId)).thenReturn(Optional.of(barber));
        when(appointmentRepository.findConflicting(eq(barberId), any(), any(), eq(null)))
                .thenReturn(Collections.emptyList());
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        Appointment result = appointmentService.create(request);

        assertThat(result.getDurationMinutes()).isEqualTo(30);
    }

    @Test
    @DisplayName("Deve validar transição CONFIRMED → IN_PROGRESS")
    void shouldTransitionFromConfirmedToInProgress() {
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setStatus(AppointmentStatus.CONFIRMED);

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        Appointment result = appointmentService.updateStatus(appointmentId, AppointmentStatus.IN_PROGRESS);

        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("Deve rejeitar transição COMPLETED → IN_PROGRESS")
    void shouldRejectInvalidTransition() {
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setStatus(AppointmentStatus.COMPLETED);

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.updateStatus(appointmentId, AppointmentStatus.IN_PROGRESS))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Transição de status inválida");
    }

    @Test
    @DisplayName("Não deve cancelar agendamento já concluído")
    void shouldNotCancelCompletedAppointment() {
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setStatus(AppointmentStatus.COMPLETED);

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.cancel(appointmentId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Não é possível cancelar");
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException para ID inexistente")
    void shouldThrowNotFoundForInvalidId() {
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.findById(appointmentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Agendamento não encontrado");
    }
}
