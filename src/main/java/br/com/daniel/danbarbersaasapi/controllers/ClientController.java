package br.com.daniel.danbarbersaasapi.controllers;

import br.com.daniel.danbarbersaasapi.domain.client.Client;
import br.com.daniel.danbarbersaasapi.domain.client.ClientRequestDTO;
import br.com.daniel.danbarbersaasapi.domain.client.ClientResponseDTO;
import br.com.daniel.danbarbersaasapi.repository.BarberRepository;
import br.com.daniel.danbarbersaasapi.repository.ClientRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/clients")
public class ClientController {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private BarberRepository barberRepository;

    @PostMapping
    public ResponseEntity<ClientResponseDTO> create(@RequestBody @Valid ClientRequestDTO data, UriComponentsBuilder uriBuilder) {
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
            var barber = barberRepository.findById(data.primaryBarberId()).orElse(null);
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
}