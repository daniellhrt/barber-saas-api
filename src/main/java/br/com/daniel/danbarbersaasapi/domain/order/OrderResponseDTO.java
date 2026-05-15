package br.com.daniel.danbarbersaasapi.domain.order;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
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
        OffsetDateTime createdAt
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