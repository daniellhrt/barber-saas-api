package br.com.daniel.danbarbersaasapi.controllers;

import br.com.daniel.danbarbersaasapi.domain.appointment.Appointment;
import br.com.daniel.danbarbersaasapi.domain.appointment.AppointmentRequestDTO;
import br.com.daniel.danbarbersaasapi.domain.appointment.AppointmentResponseDTO;
import br.com.daniel.danbarbersaasapi.domain.appointment.AppointmentStatusUpdateDTO;
import br.com.daniel.danbarbersaasapi.services.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<AppointmentResponseDTO> create(
            @RequestBody @Valid AppointmentRequestDTO data,
            UriComponentsBuilder uriBuilder) {
        Appointment appointment = appointmentService.create(data);
        var uri = uriBuilder.path("/appointments/{id}").buildAndExpand(appointment.getId()).toUri();
        return ResponseEntity.created(uri).body(new AppointmentResponseDTO(appointment));
    }

    /**
     * Busca agendamentos por período — usado para carregar o calendário.
     *
     * Exemplos:
     *   GET /appointments?startDate=2025-06-01&endDate=2025-06-30
     *   GET /appointments?startDate=2025-06-09&endDate=2025-06-09&barberId=uuid
     */
    @GetMapping
    public ResponseEntity<List<AppointmentResponseDTO>> findByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) UUID barberId) {
        ZoneId zone = ZoneId.of("America/Sao_Paulo");
        OffsetDateTime start = startDate.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime end = endDate.atTime(LocalTime.MAX).atZone(zone).toOffsetDateTime();

        List<AppointmentResponseDTO> result = appointmentService.findByPeriod(barberId, start, end)
                .stream()
                .map(AppointmentResponseDTO::new)
                .toList();
        return ResponseEntity.ok(result);
    }

    /**
     * Agenda do dia de um barbeiro específico.
     * GET /appointments/barber/{barberId}/day?date=2025-06-09
     */
    @GetMapping("/barber/{barberId}/day")
    public ResponseEntity<List<AppointmentResponseDTO>> getDaySchedule(
            @PathVariable UUID barberId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now(ZoneId.of("America/Sao_Paulo"));
        List<AppointmentResponseDTO> result = appointmentService.findByBarberAndDate(barberId, targetDate)
                .stream()
                .map(AppointmentResponseDTO::new)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(new AppointmentResponseDTO(appointmentService.findById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> update(
            @PathVariable UUID id,
            @RequestBody @Valid AppointmentRequestDTO data) {
        Appointment updated = appointmentService.update(id, data);
        return ResponseEntity.ok(new AppointmentResponseDTO(updated));
    }

    /**
     * Atualiza apenas o status do agendamento.
     * PATCH /appointments/{id}/status
     * Body: { "status": "IN_PROGRESS" }
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<AppointmentResponseDTO> updateStatus(
            @PathVariable UUID id,
            @RequestBody @Valid AppointmentStatusUpdateDTO data) {
        Appointment updated = appointmentService.updateStatus(id, data.status());
        return ResponseEntity.ok(new AppointmentResponseDTO(updated));
    }

    /**
     * Cancela um agendamento.
     * DELETE /appointments/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        appointmentService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
