package br.com.daniel.danbarbersaasapi.services;

import br.com.daniel.danbarbersaasapi.domain.barber.Barber;
import br.com.daniel.danbarbersaasapi.domain.client.Client;
import br.com.daniel.danbarbersaasapi.domain.order.*;
import br.com.daniel.danbarbersaasapi.repository.BarberRepository;
import br.com.daniel.danbarbersaasapi.repository.ClientRepository;
import br.com.daniel.danbarbersaasapi.repository.ServiceOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final ServiceOrderRepository orderRepository;

    private final ClientRepository clientRepository;

    private final BarberRepository barberRepository;

    public OrderService(ServiceOrderRepository orderRepository, ClientRepository clientRepository, BarberRepository barberRepository) {
        this.orderRepository = orderRepository;
        this.clientRepository = clientRepository;
        this.barberRepository = barberRepository;
    }

    @Transactional
    public ServiceOrder createOrder(OrderRequestDTO data) {
        Client client = clientRepository.findById(data.clientId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        Barber barber = barberRepository.findById(data.barberId())
                .orElseThrow(() -> new RuntimeException("Barbeiro não encontrado"));

        ServiceOrder order = new ServiceOrder();
        order.setClient(client);
        order.setBarber(barber);
        order.setPaymentMethod(data.paymentMethod());
        order.setNotes(data.notes());
        order.setStatus(OrderStatus.PAID); // Assumindo que o caixa registra quando é pago

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequestDTO itemDto : data.items()) {
            OrderItem item = new OrderItem();
            item.setServiceOrder(order);
            item.setReferenceId(itemDto.referenceId());
            item.setType(itemDto.type());
            item.setQuantity(itemDto.quantity());
            item.setUnitPrice(itemDto.unitPrice());

            order.getItems().add(item);

            // Calcula: (Preço Unitário * Quantidade) e soma ao total
            BigDecimal itemTotal = itemDto.unitPrice().multiply(new BigDecimal(itemDto.quantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        order.setTotalAmount(totalAmount);

        // O cascade salva a Order e os Itens automaticamente de uma vez só
        return orderRepository.save(order);
    }

    public List<ServiceOrder> getClientHistory(UUID clientId) {
        return orderRepository.findByClientIdOrderByCreatedAtDesc(clientId);
    }
}