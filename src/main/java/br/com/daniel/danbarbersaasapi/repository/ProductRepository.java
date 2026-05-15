package br.com.daniel.danbarbersaasapi.repository;

import br.com.daniel.danbarbersaasapi.domain.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
	boolean existsBySku(String sku);
}