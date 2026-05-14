package br.com.daniel.danbarbersaasapi.domain.product;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponseDTO(
        UUID id,
        String name,
        String category,
        String brand,
        BigDecimal price,
        Integer stockQuantity,
        String sku
) {
    public ProductResponseDTO(Product product) {
        this(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getBrand(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getSku()
        );
    }
}