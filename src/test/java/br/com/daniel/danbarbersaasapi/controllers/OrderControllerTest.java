package br.com.daniel.danbarbersaasapi.controllers;

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
import br.com.daniel.danbarbersaasapi.infra.exception.ErrorHandler;
import br.com.daniel.danbarbersaasapi.services.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @InjectMocks
    private OrderController orderController;

    @Mock
    private OrderService orderService;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setControllerAdvice(new ErrorHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    @DisplayName("Deve criar pedido e retornar 201 com Location")
    void createShouldReturnCreatedWithLocation() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        UUID barberId = UUID.randomUUID();

        ServiceOrder order = buildOrder(orderId, clientId, barberId, new BigDecimal("42.50"));
        when(orderService.createOrder(any(OrderRequestDTO.class))).thenReturn(order);

        OrderRequestDTO request = new OrderRequestDTO(
                clientId,
                barberId,
                "PIX",
                "Corte e barba",
                List.of(new OrderItemRequestDTO(UUID.randomUUID(), OrderItemType.SERVICE, 1, new BigDecimal("42.50")))
        );

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/orders/" + orderId))
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.clientId").value(clientId.toString()))
                .andExpect(jsonPath("$.barberId").value(barberId.toString()))
                .andExpect(jsonPath("$.paymentMethod").value("PIX"))
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    @DisplayName("Deve limitar a listagem quando o parâmetro limit for informado")
    void listAllShouldApplyLimit() throws Exception {
        UUID clientId = UUID.randomUUID();
        UUID barberId = UUID.randomUUID();
        List<ServiceOrder> orders = List.of(
                buildOrder(UUID.randomUUID(), clientId, barberId, new BigDecimal("10.00")),
                buildOrder(UUID.randomUUID(), clientId, barberId, new BigDecimal("20.00"))
        );

        when(orderService.findAllOrders()).thenReturn(orders);

        mockMvc.perform(get("/orders").param("limit", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("Deve retornar pedido pelo id")
    void getByIdShouldReturnOrder() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        UUID barberId = UUID.randomUUID();
        ServiceOrder order = buildOrder(orderId, clientId, barberId, new BigDecimal("33.30"));

        when(orderService.findById(orderId)).thenReturn(order);

        mockMvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.totalAmount").value(33.30));
    }

    @Test
    @DisplayName("Deve retornar histórico do cliente")
    void getClientHistoryShouldReturnOrders() throws Exception {
        UUID clientId = UUID.randomUUID();
        UUID barberId = UUID.randomUUID();
        List<ServiceOrder> history = List.of(buildOrder(UUID.randomUUID(), clientId, barberId, new BigDecimal("55.00")));

        when(orderService.getClientHistory(clientId)).thenReturn(history);

        mockMvc.perform(get("/orders/client/{clientId}", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].clientId").value(clientId.toString()));
    }

    private ServiceOrder buildOrder(UUID orderId, UUID clientId, UUID barberId, BigDecimal totalAmount) {
        Client client = new Client();
        client.setId(clientId);
        client.setName("Cliente Teste");

        Barber barber = new Barber();
        barber.setId(barberId);
        barber.setName("Barbeiro Teste");
        barber.setUser(new User(UUID.randomUUID(), "barber@example.com", "hashed", UserRole.BARBER, null));

        ServiceOrder order = new ServiceOrder();
        order.setId(orderId);
        order.setClient(client);
        order.setBarber(barber);
        order.setTotalAmount(totalAmount);
        order.setPaymentMethod("PIX");
        order.setStatus(OrderStatus.PAID);
        order.setCreatedAt(OffsetDateTime.of(2026, 5, 15, 10, 0, 0, 0, ZoneOffset.UTC));
        order.getItems().add(new OrderItem());
        return order;
    }
}
