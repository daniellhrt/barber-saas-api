package br.com.daniel.danbarbersaasapi.repository;

import br.com.daniel.danbarbersaasapi.domain.barber.Barber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BarberRepository extends JpaRepository<Barber, UUID> {

    @Query("SELECT b FROM Barber b WHERE b.isActive = true")
    List<Barber> findAllActive();

    /** Busca o perfil de barbeiro associado a um User — usado em GET /auth/me */
    @Query("SELECT b FROM Barber b WHERE b.user.id = :userId")
    Optional<Barber> findByUserId(@Param("userId") UUID userId);
}