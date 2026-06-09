package br.com.daniel.danbarbersaasapi.controllers;

import br.com.daniel.danbarbersaasapi.domain.service.BarberService;
import br.com.daniel.danbarbersaasapi.domain.service.ServiceRequestDTO;
import br.com.daniel.danbarbersaasapi.domain.service.ServiceResponseDTO;
import br.com.daniel.danbarbersaasapi.infra.exception.ResourceNotFoundException;
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

    @PostMapping
    public ResponseEntity<ServiceResponseDTO> create(
            @RequestBody @Valid ServiceRequestDTO data,
            UriComponentsBuilder uriBuilder) {
        BarberService newService = new BarberService();
        newService.setName(data.name());
        newService.setPrice(data.price());
        newService.setEstimatedDurationMinutes(data.estimatedDurationMinutes());
        newService.setDescription(data.description());

        repository.save(newService);

        var uri = uriBuilder.path("/services/{id}").buildAndExpand(newService.getId()).toUri();
        return ResponseEntity.created(uri).body(new ServiceResponseDTO(newService));
    }

    @GetMapping
    public ResponseEntity<List<ServiceResponseDTO>> listAll() {
        List<ServiceResponseDTO> services = repository.findAll()
                .stream()
                .map(ServiceResponseDTO::new)
                .toList();
        return ResponseEntity.ok(services);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponseDTO> getById(@PathVariable UUID id) {
        var service = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado."));
        return ResponseEntity.ok(new ServiceResponseDTO(service));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceResponseDTO> update(
            @PathVariable UUID id,
            @RequestBody @Valid ServiceRequestDTO data) {
        var service = repository.findById(id)
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
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Serviço não encontrado.");
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}