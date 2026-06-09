package br.com.daniel.danbarbersaasapi.repository;

import br.com.daniel.danbarbersaasapi.domain.appointment.Appointment;
import br.com.daniel.danbarbersaasapi.domain.appointment.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    /**
     * Busca agendamentos de um barbeiro em um período — usado para o calendário.
     */
    @Query("SELECT a FROM Appointment a " +
           "WHERE a.barber.id = :barberId " +
           "AND a.scheduledTime >= :startDate AND a.scheduledTime <= :endDate " +
           "ORDER BY a.scheduledTime ASC")
    List<Appointment> findByBarberIdAndPeriod(
            @Param("barberId") UUID barberId,
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate);

    /**
     * Busca todos os agendamentos em um período — usado para visão geral do calendário.
     */
    @Query("SELECT a FROM Appointment a " +
           "WHERE a.scheduledTime >= :startDate AND a.scheduledTime <= :endDate " +
           "ORDER BY a.scheduledTime ASC")
    List<Appointment> findByPeriod(
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate);

    /**
     * Verifica se existe agendamento conflitante para um barbeiro no horário.
     * Um conflito ocorre quando outro agendamento (não cancelado) começa antes do fim
     * do novo e termina depois do início do novo.
     */
    @Query("SELECT a FROM Appointment a " +
           "WHERE a.barber.id = :barberId " +
           "AND a.status <> 'CANCELED' " +
           "AND a.scheduledTime < :endTime " +
           "AND (FUNCTION('TIMESTAMPADD', MINUTE, a.durationMinutes, a.scheduledTime)) > :startTime " +
           "AND (:excludeId IS NULL OR a.id <> :excludeId)")
    List<Appointment> findConflicting(
            @Param("barberId") UUID barberId,
            @Param("startTime") OffsetDateTime startTime,
            @Param("endTime") OffsetDateTime endTime,
            @Param("excludeId") UUID excludeId);

    /**
     * Busca agendamentos de um cliente.
     */
    List<Appointment> findByClientIdOrderByScheduledTimeDesc(UUID clientId);
}
