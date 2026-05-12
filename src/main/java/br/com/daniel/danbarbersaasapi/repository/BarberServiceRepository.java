package br.com.daniel.danbarbersaasapi.repository;

import br.com.daniel.danbarbersaasapi.domain.service.BarberService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BarberServiceRepository extends JpaRepository<BarberService, UUID> {
    // O Spring já nos dá o .save(), .findAll(), .findById(), etc. de graça!
}