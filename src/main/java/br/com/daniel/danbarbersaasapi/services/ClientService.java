package br.com.daniel.danbarbersaasapi.services;

import br.com.daniel.danbarbersaasapi.domain.client.Client;
import br.com.daniel.danbarbersaasapi.domain.client.ClientRequestDTO;
import br.com.daniel.danbarbersaasapi.domain.client.ClientResponseDTO;
import br.com.daniel.danbarbersaasapi.domain.client.ClientUpdateDTO;
import br.com.daniel.danbarbersaasapi.infra.exception.ConflictException;
import br.com.daniel.danbarbersaasapi.infra.exception.ResourceNotFoundException;
import br.com.daniel.danbarbersaasapi.repository.BarberRepository;
import br.com.daniel.danbarbersaasapi.repository.ClientRepository;
import br.com.daniel.danbarbersaasapi.repository.ServiceOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final BarberRepository barberRepository;
    private final ServiceOrderRepository serviceOrderRepository;

    @Transactional
    public Client create(ClientRequestDTO data) {
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

        if (data.primaryBarberId() != null) {
            var barber = barberRepository.findById(data.primaryBarberId())
                    .orElseThrow(() -> new ResourceNotFoundException("Barbeiro não encontrado."));
            client.setPrimaryBarber(barber);
        }

        return clientRepository.save(client);
    }

    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    public Client findById(UUID id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado."));
    }

    @Transactional
    public Client update(UUID id, ClientUpdateDTO data) {
        Client client = findById(id);

        if (data.email() != null && !data.email().equals(client.getEmail())
                && clientRepository.existsByEmail(data.email())) {
            throw new ConflictException("E-mail já cadastrado para outro cliente.");
        }
        if (data.cpf() != null && !data.cpf().equals(client.getCpf())
                && clientRepository.existsByCpf(data.cpf())) {
            throw new ConflictException("CPF já cadastrado para outro cliente.");
        }

        client.updateInfo(data);
        return clientRepository.save(client);
    }

    @Transactional
    public void delete(UUID id) {
        Client client = findById(id);
        clientRepository.delete(client);
    }

    /**
     * Retorna clientes cujo intervalo de retorno esperado foi ultrapassado.
     * Um cliente é considerado "overdue" quando:
     *   - Tem returnIntervalDays definido, E
     *   - Sua última visita (ServiceOrder mais recente) foi há mais dias do que o intervalo
     */
    public List<ClientResponseDTO> findOverdueClients() {
        ZoneId zone = ZoneId.of("America/Sao_Paulo");
        OffsetDateTime now = OffsetDateTime.now(zone);

        return clientRepository.findAll().stream()
                .filter(client -> client.getReturnIntervalDays() != null && client.getReturnIntervalDays() > 0)
                .map(client -> {
                    var orders = serviceOrderRepository.findByClientIdOrderByCreatedAtDesc(client.getId());
                    if (orders.isEmpty()) {
                        // Nunca veio — já está overdue desde o cadastro
                        long daysSince = java.time.temporal.ChronoUnit.DAYS.between(
                                client.getCreatedAt(), now);
                        if (daysSince >= client.getReturnIntervalDays()) {
                            return new ClientResponseDTO(client, (int) daysSince);
                        }
                        return null;
                    }
                    OffsetDateTime lastVisit = orders.get(0).getCreatedAt();
                    long daysSinceLastVisit = java.time.temporal.ChronoUnit.DAYS.between(lastVisit, now);
                    if (daysSinceLastVisit >= client.getReturnIntervalDays()) {
                        return new ClientResponseDTO(client, (int) daysSinceLastVisit);
                    }
                    return null;
                })
                .filter(dto -> dto != null)
                .toList();
    }
}
