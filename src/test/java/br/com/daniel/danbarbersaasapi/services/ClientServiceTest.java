package br.com.daniel.danbarbersaasapi.services;

import br.com.daniel.danbarbersaasapi.domain.client.Client;
import br.com.daniel.danbarbersaasapi.domain.client.ClientRequestDTO;
import br.com.daniel.danbarbersaasapi.domain.client.ClientUpdateDTO;
import br.com.daniel.danbarbersaasapi.infra.exception.ConflictException;
import br.com.daniel.danbarbersaasapi.infra.exception.ResourceNotFoundException;
import br.com.daniel.danbarbersaasapi.repository.BarberRepository;
import br.com.daniel.danbarbersaasapi.repository.ClientRepository;
import br.com.daniel.danbarbersaasapi.repository.ServiceOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private BarberRepository barberRepository;

    @Mock
    private ServiceOrderRepository serviceOrderRepository;

    @InjectMocks
    private ClientService clientService;

    private ClientRequestDTO validRequest;
    private UUID clientId;
    private Client existingClient;

    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();

        validRequest = new ClientRequestDTO(
                "João Silva",
                "11999999999",
                "11999999999",
                "joao@email.com",
                "123.456.789-00",
                null, // birthDate
                "Cliente VIP",
                "Rua A, 123",
                null  // primaryBarberId
        );

        existingClient = new Client();
        existingClient.setId(clientId);
        existingClient.setName("João Silva");
        existingClient.setPhone("11999999999");
        existingClient.setEmail("joao@email.com");
        existingClient.setCpf("123.456.789-00");
    }

    @Test
    @DisplayName("Deve criar cliente com dados válidos")
    void shouldCreateClientWithValidData() {
        when(clientRepository.existsByEmail("joao@email.com")).thenReturn(false);
        when(clientRepository.existsByCpf("123.456.789-00")).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> {
            Client saved = invocation.getArgument(0);
            saved.setId(clientId);
            return saved;
        });

        Client result = clientService.create(validRequest);

        assertThat(result.getName()).isEqualTo("João Silva");
        assertThat(result.getEmail()).isEqualTo("joao@email.com");
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    @DisplayName("Deve lançar ConflictException para email duplicado")
    void shouldThrowConflictForDuplicateEmail() {
        when(clientRepository.existsByEmail("joao@email.com")).thenReturn(true);

        assertThatThrownBy(() -> clientService.create(validRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("E-mail já cadastrado");
    }

    @Test
    @DisplayName("Deve lançar ConflictException para CPF duplicado")
    void shouldThrowConflictForDuplicateCpf() {
        when(clientRepository.existsByEmail("joao@email.com")).thenReturn(false);
        when(clientRepository.existsByCpf("123.456.789-00")).thenReturn(true);

        assertThatThrownBy(() -> clientService.create(validRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("CPF já cadastrado");
    }

    @Test
    @DisplayName("Deve buscar cliente por ID")
    void shouldFindClientById() {
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(existingClient));

        Client result = clientService.findById(clientId);

        assertThat(result.getId()).isEqualTo(clientId);
        assertThat(result.getName()).isEqualTo("João Silva");
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException para ID inexistente")
    void shouldThrowNotFoundForInvalidId() {
        UUID invalidId = UUID.randomUUID();
        when(clientRepository.findById(invalidId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.findById(invalidId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cliente não encontrado");
    }

    @Test
    @DisplayName("Deve atualizar dados do cliente")
    void shouldUpdateClient() {
        ClientUpdateDTO updateData = new ClientUpdateDTO(
                "João Atualizado", "11888888888", "11888888888",
                "joao.novo@email.com", null, null, null, null, 30
        );

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(existingClient));
        when(clientRepository.existsByEmail("joao.novo@email.com")).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenAnswer(inv -> inv.getArgument(0));

        Client result = clientService.update(clientId, updateData);

        assertThat(result.getName()).isEqualTo("João Atualizado");
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    @DisplayName("Deve deletar cliente existente")
    void shouldDeleteClient() {
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(existingClient));

        clientService.delete(clientId);

        verify(clientRepository).delete(existingClient);
    }
}
