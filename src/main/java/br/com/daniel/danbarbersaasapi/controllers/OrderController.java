package br.com.daniel.danbarbersaasapi.controllers;

import br.com.daniel.danbarbersaasapi.domain.order.OrderRequestDTO;
import br.com.daniel.danbarbersaasapi.domain.order.OrderResponseDTO;
import br.com.daniel.danbarbersaasapi.domain.order.ServiceOrder;
import br.com.daniel.danbarbersaasapi.services.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDTO> create(@RequestBody @Valid OrderRequestDTO data, UriComponentsBuilder uriBuilder) {
        ServiceOrder newOrder = orderService.createOrder(data);

        var uri = uriBuilder.path("/orders/{id}").buildAndExpand(newOrder.getId()).toUri();
        return ResponseEntity.created(uri).body(new OrderResponseDTO(newOrder));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> listAll(@RequestParam(required = false) Integer limit) {
        List<OrderResponseDTO> orders = orderService.findAllOrders()
                .stream()
                .map(OrderResponseDTO::new)
                .toList();

        if (limit != null && limit > 0 && orders.size() > limit) {
            orders = orders.subList(0, limit);
        }

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(new OrderResponseDTO(orderService.findById(id)));
    }

    // Endpoint de ouro para resolver o seu problema: O histórico do cliente
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<OrderResponseDTO>> getClientHistory(@PathVariable UUID clientId) {
        List<OrderResponseDTO> history = orderService.getClientHistory(clientId)
                .stream()
                .map(OrderResponseDTO::new)
                .toList();

        return ResponseEntity.ok(history);
    }
}