package br.com.daniel.danbarbersaasapi.domain.product;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponseDTO(UUID id, String name, BigDecimal price, String description) {
    public ProductResponseDTO(Product product) {
        this(product.getId(), product.getName(), product.getPrice(), product.getDescription());
    }
}