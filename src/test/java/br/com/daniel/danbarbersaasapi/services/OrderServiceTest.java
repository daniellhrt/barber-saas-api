package br.com.daniel.danbarbersaasapi.services;

import br.com.daniel.danbarbersaasapi.domain.barber.Barber;
import br.com.daniel.danbarbersaasapi.domain.client.Client;
import br.com.daniel.danbarbersaasapi.domain.order.*;
import br.com.daniel.danbarbersaasapi.infra.exception.BusinessException;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    private UUID clientId;
    private UUID barberId;
    private UUID orderId;
    private Client client;
    private Barber barber;

    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();
        barberId = UUID.randomUUID();
        orderId = UUID.randomUUID();

        client = new Client();
        client.setId(clientId);
        client.setName("João");

        barber = new Barber();
        barber.setId(barberId);
        barber.setName("Carlos");
    }

    @Test
    @DisplayName("Deve criar pedido com itens e calcular total corretamente")
    void shouldCreateOrderWithItemsAndCalculateTotal() {
        var items = List.of(
                new OrderItemRequestDTO(UUID.randomUUID(), OrderItemType.SERVICE, 1, new BigDecimal("50.00")),
                new OrderItemRequestDTO(UUID.randomUUID(), OrderItemType.PRODUCT, 2, new BigDecimal("25.00"))
        );
        var request = new OrderRequestDTO(clientId, barberId, "PIX", "Atendimento padrão", items);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(barberRepository.findById(barberId)).thenReturn(Optional.of(barber));
        when(orderRepository.save(any(ServiceOrder.class))).thenAnswer(inv -> {
            ServiceOrder order = inv.getArgument(0);
            order.setId(orderId);
            return order;
        });

        ServiceOrder result = orderService.createOrder(request);

        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getClient().getName()).isEqualTo("João");
        verify(orderRepository).save(any(ServiceOrder.class));
    }

    @Test
    @DisplayName("Deve lançar exceção se pedido não tiver itens")
    void shouldThrowWhenNoItems() {
        var request = new OrderRequestDTO(clientId, barberId, "PIX", null, List.of());

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(barberRepository.findById(barberId)).thenReturn(Optional.of(barber));

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ao menos um item");
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException para cliente inexistente")
    void shouldThrowForInvalidClient() {
        var request = new OrderRequestDTO(clientId, barberId, "PIX", null,
                List.of(new OrderItemRequestDTO(UUID.randomUUID(), OrderItemType.SERVICE, 1, new BigDecimal("30.00"))));

        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cliente não encontrado");
    }

    @Test
    @DisplayName("Deve atualizar pedido recalculando total")
    void shouldUpdateOrderRecalculatingTotal() {
        ServiceOrder existingOrder = new ServiceOrder();
        existingOrder.setId(orderId);
        existingOrder.setClient(client);
        existingOrder.setBarber(barber);
        existingOrder.setTotalAmount(new BigDecimal("50.00"));

        var newItems = List.of(
                new OrderItemRequestDTO(UUID.randomUUID(), OrderItemType.SERVICE, 1, new BigDecimal("80.00"))
        );
        var updateRequest = new OrderRequestDTO(clientId, barberId, "CARTÃO", null, newItems);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(ServiceOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        ServiceOrder result = orderService.updateOrder(orderId, updateRequest);

        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("80.00"));
        assertThat(result.getPaymentMethod()).isEqualTo("CARTÃO");
    }

    @Test
    @DisplayName("Deve deletar pedido existente")
    void shouldDeleteOrder() {
        ServiceOrder order = new ServiceOrder();
        order.setId(orderId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        orderService.deleteOrder(orderId);

        verify(orderRepository).delete(order);
    }
}
