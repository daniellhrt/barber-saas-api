package br.com.daniel.danbarbersaasapi.controllers;

import br.com.daniel.danbarbersaasapi.domain.barber.Barber;
import br.com.daniel.danbarbersaasapi.domain.service.BarberService;
import br.com.daniel.danbarbersaasapi.domain.service.ServiceRequestDTO;
import br.com.daniel.danbarbersaasapi.domain.service.ServiceResponseDTO;
import br.com.daniel.danbarbersaasapi.infra.exception.ResourceNotFoundException;
import br.com.daniel.danbarbersaasapi.infra.security.TenantContext;
import br.com.daniel.danbarbersaasapi.repository.BarberServiceRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
public class ServiceController {

    private final BarberServiceRepository repository;
    private final TenantContext tenantContext;

    @PostMapping
    public ResponseEntity<ServiceResponseDTO> create(
            @RequestBody @Valid ServiceRequestDTO data,
            UriComponentsBuilder uriBuilder) {
        Barber owner = tenantContext.getCurrentBarber();

        BarberService newService = new BarberService();
        newService.setName(data.name());
        newService.setPrice(data.price());
        newService.setEstimatedDurationMinutes(data.estimatedDurationMinutes());
        newService.setDescription(data.description());
        newService.setOwnerBarber(owner);

        repository.save(newService);

        var uri = uriBuilder.path("/services/{id}").buildAndExpand(newService.getId()).toUri();
        return ResponseEntity.created(uri).body(new ServiceResponseDTO(newService));
    }

    @GetMapping
    public ResponseEntity<List<ServiceResponseDTO>> listAll() {
        Barber owner = tenantContext.getCurrentBarber();

        List<ServiceResponseDTO> services = repository.findByOwnerBarberId(owner.getId())
                .stream()
                .map(ServiceResponseDTO::new)
                .toList();
        return ResponseEntity.ok(services);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponseDTO> getById(@PathVariable UUID id) {
        Barber owner = tenantContext.getCurrentBarber();

        var service = repository.findByIdAndOwnerBarberId(id, owner.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado."));
        return ResponseEntity.ok(new ServiceResponseDTO(service));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceResponseDTO> update(
            @PathVariable UUID id,
            @RequestBody @Valid ServiceRequestDTO data) {
        Barber owner = tenantContext.getCurrentBarber();

        var service = repository.findByIdAndOwnerBarberId(id, owner.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado."));

        service.setName(data.name());
        service.setPrice(data.price());
        service.setEstimatedDurationMinutes(data.estimatedDurationMinutes());
        service.setDescription(data.description());

        repository.save(service);
        return ResponseEntity.ok(new ServiceResponseDTO(service));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        Barber owner = tenantContext.getCurrentBarber();

        var service = repository.findByIdAndOwnerBarberId(id, owner.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado."));

        repository.delete(service);
        return ResponseEntity.noContent().build();
    }
}