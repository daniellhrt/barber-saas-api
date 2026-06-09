package br.com.daniel.danbarbersaasapi.services;

import br.com.daniel.danbarbersaasapi.domain.barber.Barber;
import br.com.daniel.danbarbersaasapi.domain.client.Client;
import br.com.daniel.danbarbersaasapi.domain.order.*;
import br.com.daniel.danbarbersaasapi.infra.exception.BusinessException;
import br.com.daniel.danbarbersaasapi.infra.exception.ResourceNotFoundException;
import br.com.daniel.danbarbersaasapi.repository.BarberRepository;
import br.com.daniel.danbarbersaasapi.repository.ClientRepository;
import br.com.daniel.danbarbersaasapi.repository.ServiceOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private ServiceOrderRepository orderRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private BarberRepository barberRepository;

    @Transactional
    public ServiceOrder createOrder(OrderRequestDTO data) {
        Client client = clientRepository.findById(data.clientId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado."));

        Barber barber = barberRepository.findById(data.barberId())
                .orElseThrow(() -> new ResourceNotFoundException("Barbeiro não encontrado."));

        if (data.items() == null || data.items().isEmpty()) {
            throw new BusinessException("O pedido deve conter ao menos um item.");
        }

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

    public List<ServiceOrder> findAllOrders() {
        return orderRepository.findAll();
    }

    public ServiceOrder findById(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado."));
    }

    @Transactional
    public ServiceOrder updateOrder(UUID id, OrderRequestDTO data) {
        ServiceOrder order = findById(id);

        order.setPaymentMethod(data.paymentMethod());
        order.setNotes(data.notes());

        // Recalcula itens e total se novos itens foram enviados
        if (data.items() != null && !data.items().isEmpty()) {
            order.getItems().clear();

            BigDecimal totalAmount = BigDecimal.ZERO;
            for (OrderItemRequestDTO itemDto : data.items()) {
                OrderItem item = new OrderItem();
                item.setServiceOrder(order);
                item.setReferenceId(itemDto.referenceId());
                item.setType(itemDto.type());
                item.setQuantity(itemDto.quantity());
                item.setUnitPrice(itemDto.unitPrice());
                order.getItems().add(item);

                BigDecimal itemTotal = itemDto.unitPrice().multiply(new BigDecimal(itemDto.quantity()));
                totalAmount = totalAmount.add(itemTotal);
            }
            order.setTotalAmount(totalAmount);
        }

        return orderRepository.save(order);
    }

    @Transactional
    public void deleteOrder(UUID id) {
        ServiceOrder order = findById(id);
        orderRepository.delete(order);
    }
}