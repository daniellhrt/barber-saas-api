package br.com.daniel.danbarbersaasapi.controllers;

import br.com.daniel.danbarbersaasapi.domain.order.OrderRequestDTO;
import br.com.daniel.danbarbersaasapi.domain.order.OrderResponseDTO;
import br.com.daniel.danbarbersaasapi.domain.order.ServiceOrder;
import br.com.daniel.danbarbersaasapi.services.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDTO> create(
            @RequestBody @Valid OrderRequestDTO data,
            UriComponentsBuilder uriBuilder) {
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

        // TODO: Substituir por Pageable (Fase 3 — paginação)
        if (limit != null && limit > 0 && orders.size() > limit) {
            orders = orders.subList(0, limit);
        }

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(new OrderResponseDTO(orderService.findById(id)));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<OrderResponseDTO>> getClientHistory(@PathVariable UUID clientId) {
        List<OrderResponseDTO> history = orderService.getClientHistory(clientId)
                .stream()
                .map(OrderResponseDTO::new)
                .toList();
        return ResponseEntity.ok(history);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> update(
            @PathVariable UUID id,
            @RequestBody @Valid OrderRequestDTO data) {
        // @Transactional está em OrderService.updateOrder — não deve ficar no controller
        ServiceOrder updated = orderService.updateOrder(id, data);
        return ResponseEntity.ok(new OrderResponseDTO(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        // @Transactional está em OrderService.deleteOrder
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}