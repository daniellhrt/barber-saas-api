package br.com.daniel.danbarbersaasapi.services;

import br.com.daniel.danbarbersaasapi.domain.barber.Barber;
import br.com.daniel.danbarbersaasapi.domain.client.Client;
import br.com.daniel.danbarbersaasapi.domain.order.OrderItem;
import br.com.daniel.danbarbersaasapi.domain.order.OrderItemRequestDTO;
import br.com.daniel.danbarbersaasapi.domain.order.OrderItemType;
import br.com.daniel.danbarbersaasapi.domain.order.OrderRequestDTO;
import br.com.daniel.danbarbersaasapi.domain.order.OrderStatus;
import br.com.daniel.danbarbersaasapi.domain.order.ServiceOrder;
import br.com.daniel.danbarbersaasapi.domain.user.User;
import br.com.daniel.danbarbersaasapi.domain.user.UserRole;
import br.com.daniel.danbarbersaasapi.infra.exception.BusinessException;
import br.com.daniel.danbarbersaasapi.infra.exception.ResourceNotFoundException;
import br.com.daniel.danbarbersaasapi.repository.BarberRepository;
import br.com.daniel.danbarbersaasapi.repository.ClientRepository;
import br.com.daniel.danbarbersaasapi.repository.ServiceOrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private ServiceOrderRepository orderRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private BarberRepository barberRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("Deve criar pedido com itens e total calculado corretamente")
    void createOrderShouldPersistOrderWithCalculatedTotal() {
        UUID clientId = UUID.randomUUID();
        UUID barberId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Client client = buildClient(clientId, "Cliente Teste");
        Barber barber = buildBarber(barberId, "Barbeiro Teste");

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(barberRepository.findById(barberId)).thenReturn(Optional.of(barber));
        when(orderRepository.save(any(ServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderRequestDTO request = new OrderRequestDTO(
                clientId,
                barberId,
                "PIX",
                "Observações do pedido",
                List.of(
                        new OrderItemRequestDTO(serviceId, OrderItemType.SERVICE, 2, new BigDecimal("35.50")),
                        new OrderItemRequestDTO(productId, OrderItemType.PRODUCT, 1, new BigDecimal("20.00"))
                )
        );

        ServiceOrder createdOrder = orderService.createOrder(request);

        assertThat(createdOrder.getClient()).isSameAs(client);
        assertThat(createdOrder.getBarber()).isSameAs(barber);
        assertThat(createdOrder.getPaymentMethod()).isEqualTo("PIX");
        assertThat(createdOrder.getNotes()).isEqualTo("Observações do pedido");
        assertThat(createdOrder.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(createdOrder.getTotalAmount()).isEqualByComparingTo("91.00");
        assertThat(createdOrder.getItems()).hasSize(2);
        assertThat(createdOrder.getItems())
                .allSatisfy(item -> assertThat(item.getServiceOrder()).isSameAs(createdOrder));

        ArgumentCaptor<ServiceOrder> captor = ArgumentCaptor.forClass(ServiceOrder.class);
        verify(orderRepository).save(captor.capture());
        assertThat(captor.getValue().getItems()).hasSize(2);
        assertThat(captor.getValue().getTotalAmount()).isEqualByComparingTo("91.00");
    }

    @Test
    @DisplayName("Deve rejeitar pedido sem itens")
    void createOrderShouldRejectEmptyItems() {
        UUID clientId = UUID.randomUUID();
        UUID barberId = UUID.randomUUID();
        OrderRequestDTO request = new OrderRequestDTO(clientId, barberId, "DINHEIRO", null, List.of());

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(buildClient(clientId, "Cliente Teste")));
        when(barberRepository.findById(barberId)).thenReturn(Optional.of(buildBarber(barberId, "Barbeiro Teste")));

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("O pedido deve conter ao menos um item.");

        verifyNoInteractions(orderRepository);
    }

    @Test
    @DisplayName("Deve lançar erro quando o cliente não existir")
    void createOrderShouldThrowWhenClientIsMissing() {
        UUID clientId = UUID.randomUUID();
        UUID barberId = UUID.randomUUID();
        OrderRequestDTO request = new OrderRequestDTO(
                clientId,
                barberId,
                "CARTAO",
                null,
                List.of(new OrderItemRequestDTO(UUID.randomUUID(), OrderItemType.SERVICE, 1, new BigDecimal("25.00")))
        );

        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Cliente não encontrado.");

        verify(clientRepository).findById(clientId);
        verify(barberRepository, never()).findById(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve retornar o histórico do cliente em ordem decrescente")
    void getClientHistoryShouldReturnRepositoryResult() {
        UUID clientId = UUID.randomUUID();
        List<ServiceOrder> orders = List.of(new ServiceOrder(), new ServiceOrder());
        when(orderRepository.findByClientIdOrderByCreatedAtDesc(clientId)).thenReturn(orders);

        List<ServiceOrder> result = orderService.getClientHistory(clientId);

        assertThat(result).isSameAs(orders);
        verify(orderRepository).findByClientIdOrderByCreatedAtDesc(clientId);
    }

    private Client buildClient(UUID id, String name) {
        Client client = new Client();
        client.setId(id);
        client.setName(name);
        client.setCpf("12345678901");
        client.setEmail(name.toLowerCase().replace(' ', '.') + "@example.com");
        client.setBirthDate(LocalDate.of(1995, 5, 15));
        return client;
    }

    private Barber buildBarber(UUID id, String name) {
        Barber barber = new Barber();
        barber.setId(id);
        barber.setUser(new User(UUID.randomUUID(), name.toLowerCase().replace(' ', '.') + "@example.com", "hashed", UserRole.BARBER, null));
        barber.setName(name);
        return barber;
    }
}


