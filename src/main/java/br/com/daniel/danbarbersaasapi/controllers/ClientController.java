package br.com.daniel.danbarbersaasapi.controllers;

import br.com.daniel.danbarbersaasapi.domain.client.Client;
import br.com.daniel.danbarbersaasapi.domain.client.ClientRequestDTO;
import br.com.daniel.danbarbersaasapi.domain.client.ClientResponseDTO;
import br.com.daniel.danbarbersaasapi.domain.client.ClientUpdateDTO;
import br.com.daniel.danbarbersaasapi.infra.exception.ConflictException;
import br.com.daniel.danbarbersaasapi.infra.exception.ResourceNotFoundException;
import br.com.daniel.danbarbersaasapi.repository.BarberRepository;
import br.com.daniel.danbarbersaasapi.repository.ClientRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/clients")
public class ClientController {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private BarberRepository barberRepository;

    @PostMapping
    @Transactional
    public ResponseEntity<ClientResponseDTO> create(@RequestBody @Valid ClientRequestDTO data, UriComponentsBuilder uriBuilder) {
        if (data.email() != null && clientRepository.existsByEmail(data.email())) {
            throw new ConflictException("E-mail já cadastrado para outro cliente.");
        }

        if (data.cpf() != null && clientRepository.existsByCpf(data.cpf())) {
            throw new ConflictException("CPF já cadastrado para outro cliente.");
        }

        Client client = new Client();
        client.setName(data.name());
        client.setPhone(data.phone());
        client.setWhatsapp(data.whatsapp());
        client.setEmail(data.email());
        client.setCpf(data.cpf());
        client.setBirthDate(data.birthDate());
        client.setNotes(data.notes());
        client.setAddress(data.address());

        // Se veio um ID de barbeiro na requisição, buscamos no banco e vinculamos
        if (data.primaryBarberId() != null) {
            var barber = barberRepository.findById(data.primaryBarberId())
                    .orElseThrow(() -> new ResourceNotFoundException("Barbeiro não encontrado."));
            client.setPrimaryBarber(barber);
        }

        clientRepository.save(client);

        var uri = uriBuilder.path("/clients/{id}").buildAndExpand(client.getId()).toUri();
        return ResponseEntity.created(uri).body(new ClientResponseDTO(client));
    }

    @GetMapping
    public ResponseEntity<List<ClientResponseDTO>> listAll() {
        List<ClientResponseDTO> clients = clientRepository.findAll()
                .stream()
                .map(ClientResponseDTO::new)
                .toList();

        return ResponseEntity.ok(clients);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> getById(@PathVariable UUID id) {
        var client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado."));

        return ResponseEntity.ok(new ClientResponseDTO(client));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ClientResponseDTO> update(@PathVariable UUID id, @RequestBody @Valid ClientUpdateDTO data) {
        var client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado."));

        if (data.email() != null && !data.email().equals(client.getEmail()) && clientRepository.existsByEmail(data.email())) {
            throw new ConflictException("E-mail já cadastrado para outro cliente.");
        }

        if (data.cpf() != null && !data.cpf().equals(client.getCpf()) && clientRepository.existsByCpf(data.cpf())) {
            throw new ConflictException("CPF já cadastrado para outro cliente.");
        }

        client.updateInfo(data);
        return ResponseEntity.ok(new ClientResponseDTO(client));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        var client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado."));

        clientRepository.delete(client);
        return ResponseEntity.noContent().build();
    }
}