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

    /**
     * Retorna link do WhatsApp formatado com mensagem personalizada de retorno.
     * Exemplo: https://wa.me/5511999999999?text=Oi%20João%2C%20sentimos%20sua%20falta!
     */
    @GetMapping("/{id}/whatsapp-link")
    public ResponseEntity<WhatsAppLinkResponse> getWhatsappLink(@PathVariable UUID id) {
        Client client = clientService.findById(id);
        String phone = client.getWhatsapp() != null ? client.getWhatsapp() : client.getPhone();

        if (phone == null || phone.isBlank()) {
            return ResponseEntity.ok(new WhatsAppLinkResponse(null, "Cliente não possui telefone/WhatsApp cadastrado."));
        }

        String cleanPhone = phone.replaceAll("[^\\d]", "");
        if (!cleanPhone.startsWith("55")) {
            cleanPhone = "55" + cleanPhone;
        }

        String message = java.net.URLEncoder.encode(
                "Oi " + client.getName() + ", tudo bem? Faz um tempo que não te vemos por aqui! " +
                "Que tal agendar um horário? 💈",
                java.nio.charset.StandardCharsets.UTF_8
        );

        String link = "https://wa.me/" + cleanPhone + "?text=" + message;
        return ResponseEntity.ok(new WhatsAppLinkResponse(link, null));
    }

    /** DTO simples para resposta do WhatsApp link */
    public record WhatsAppLinkResponse(String whatsappLink, String error) {}
}