package br.com.daniel.danbarbersaasapi.repository;

import br.com.daniel.danbarbersaasapi.domain.barber.Barber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface BarberRepository extends JpaRepository<Barber, UUID> {

    @Query("SELECT b FROM Barber b WHERE b.isActive = true")
    List<Barber> findAllActive();
}