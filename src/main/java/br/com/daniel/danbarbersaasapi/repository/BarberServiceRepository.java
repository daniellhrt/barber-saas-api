package br.com.daniel.danbarbersaasapi.repository;

import br.com.daniel.danbarbersaasapi.domain.service.BarberService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BarberServiceRepository extends JpaRepository<BarberService, UUID> {

    /** Lista todos os serviços do dono (tenant) */
    List<BarberService> findByOwnerBarberId(UUID ownerBarberId);

    /** Busca serviço por ID e dono — garante isolamento multi-tenant */
    Optional<BarberService> findByIdAndOwnerBarberId(UUID id, UUID ownerBarberId);
}