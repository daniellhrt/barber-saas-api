package br.com.daniel.danbarbersaasapi.repository;

import br.com.daniel.danbarbersaasapi.domain.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    boolean existsBySku(String sku);

    /** Lista todos os produtos do dono (tenant) */
    List<Product> findByOwnerBarberId(UUID ownerBarberId);

    /** Busca produto por ID e dono — garante isolamento multi-tenant */
    Optional<Product> findByIdAndOwnerBarberId(UUID id, UUID ownerBarberId);

    /** Verifica SKU duplicado apenas dentro do mesmo tenant */
    boolean existsBySkuAndOwnerBarberId(String sku, UUID ownerBarberId);
}