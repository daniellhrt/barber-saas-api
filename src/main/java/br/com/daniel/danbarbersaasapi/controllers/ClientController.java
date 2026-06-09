package br.com.daniel.danbarbersaasapi.controllers;

import br.com.daniel.danbarbersaasapi.domain.client.Client;
import br.com.daniel.danbarbersaasapi.domain.client.ClientRequestDTO;
import br.com.daniel.danbarbersaasapi.domain.client.ClientResponseDTO;
import br.com.daniel.danbarbersaasapi.domain.client.ClientUpdateDTO;
import br.com.daniel.danbarbersaasapi.services.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    public ResponseEntity<ClientResponseDTO> create(
            @RequestBody @Valid ClientRequestDTO data,
            UriComponentsBuilder uriBuilder) {
        Client client = clientService.create(data);
        var uri = uriBuilder.path("/clients/{id}").buildAndExpand(client.getId()).toUri();
        return ResponseEntity.created(uri).body(new ClientResponseDTO(client));
    }

    @GetMapping
    public ResponseEntity<List<ClientResponseDTO>> listAll() {
        List<ClientResponseDTO> clients = clientService.findAll()
                .stream()
                .map(ClientResponseDTO::new)
                .toList();
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(new ClientResponseDTO(clientService.findById(id)));
    }

    /**
     * Lista clientes que ultrapassaram o intervalo de retorno esperado.
     * Usado para o painel de controle de retorno do barbeiro.
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<ClientResponseDTO>> getOverdueClients() {
        return ResponseEntity.ok(clientService.findOverdueClients());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> update(
            @PathVariable UUID id,
            @RequestBody @Valid ClientUpdateDTO data) {
        Client updated = clientService.update(id, data);
        return ResponseEntity.ok(new ClientResponseDTO(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        clientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}