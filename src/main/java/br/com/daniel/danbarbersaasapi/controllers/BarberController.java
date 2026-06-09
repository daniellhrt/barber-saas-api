package br.com.daniel.danbarbersaasapi.controllers;

import br.com.daniel.danbarbersaasapi.domain.barber.Barber;
import br.com.daniel.danbarbersaasapi.domain.barber.BarberRequestDTO;
import br.com.daniel.danbarbersaasapi.domain.barber.BarberResponseDTO;
import br.com.daniel.danbarbersaasapi.services.BarberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/barbers")
@RequiredArgsConstructor
public class BarberController {

    private final BarberService barberService;

    @PostMapping
    public ResponseEntity<BarberResponseDTO> create(
            @RequestBody @Valid BarberRequestDTO data,
            UriComponentsBuilder uriBuilder) {
        Barber barber = barberService.create(data);
        var uri = uriBuilder.path("/barbers/{id}").buildAndExpand(barber.getId()).toUri();
        return ResponseEntity.created(uri).body(new BarberResponseDTO(barber));
    }

    @GetMapping
    public ResponseEntity<List<BarberResponseDTO>> listAll() {
        var barbers = barberService.findAll().stream().map(BarberResponseDTO::new).toList();
        return ResponseEntity.ok(barbers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BarberResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(new BarberResponseDTO(barberService.findById(id)));
    }
}