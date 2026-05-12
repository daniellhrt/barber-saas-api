package br.com.daniel.danbarbersaasapi.domain.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderResponseDTO(
        UUID id,
        UUID clientId,
        String clientName,
        UUID barberId,
        String barberName,
        BigDecimal totalAmount,
        String paymentMethod,
        OrderStatus status,
        LocalDateTime createdAt
) {
    public OrderResponseDTO(ServiceOrder order) {
        this(
                order.getId(),
                order.getClient().getId(),
                order.getClient().getName(),
                order.getBarber().getId(),
                order.getBarber().getName(),
                order.getTotalAmount(),
                order.getPaymentMethod(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
}