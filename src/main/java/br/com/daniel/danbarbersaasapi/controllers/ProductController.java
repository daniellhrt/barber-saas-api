package br.com.daniel.danbarbersaasapi.controllers;

import br.com.daniel.danbarbersaasapi.domain.barber.Barber;
import br.com.daniel.danbarbersaasapi.domain.product.Product;
import br.com.daniel.danbarbersaasapi.domain.product.ProductRequestDTO;
import br.com.daniel.danbarbersaasapi.domain.product.ProductResponseDTO;
import br.com.daniel.danbarbersaasapi.infra.exception.ConflictException;
import br.com.daniel.danbarbersaasapi.infra.exception.ResourceNotFoundException;
import br.com.daniel.danbarbersaasapi.infra.security.TenantContext;
import br.com.daniel.danbarbersaasapi.repository.ProductRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository repository;
    private final TenantContext tenantContext;

    @PostMapping
    public ResponseEntity<ProductResponseDTO> create(
            @RequestBody @Valid ProductRequestDTO data,
            UriComponentsBuilder uriBuilder) {
        Barber owner = tenantContext.getCurrentBarber();

        if (data.sku() != null && repository.existsBySkuAndOwnerBarberId(data.sku(), owner.getId())) {
            throw new ConflictException("SKU já cadastrado para outro produto.");
        }

        Product product = new Product();
        product.setName(data.name());
        product.setCategory(data.category());
        product.setBrand(data.brand());
        product.setPrice(data.price());
        if (data.stockQuantity() != null) {
            product.setStockQuantity(data.stockQuantity());
        }
        product.setSku(data.sku());
        product.setOwnerBarber(owner);

        repository.save(product);

        var uri = uriBuilder.path("/products/{id}").buildAndExpand(product.getId()).toUri();
        return ResponseEntity.created(uri).body(new ProductResponseDTO(product));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> listAll() {
        Barber owner = tenantContext.getCurrentBarber();

        var products = repository.findByOwnerBarberId(owner.getId())
                .stream()
                .map(ProductResponseDTO::new)
                .toList();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getById(@PathVariable UUID id) {
        Barber owner = tenantContext.getCurrentBarber();

        var product = repository.findByIdAndOwnerBarberId(id, owner.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado."));
        return ResponseEntity.ok(new ProductResponseDTO(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> update(
            @PathVariable UUID id,
            @RequestBody @Valid ProductRequestDTO data) {
        Barber owner = tenantContext.getCurrentBarber();

        var product = repository.findByIdAndOwnerBarberId(id, owner.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado."));

        if (data.sku() != null && !data.sku().equals(product.getSku())
                && repository.existsBySkuAndOwnerBarberId(data.sku(), owner.getId())) {
            throw new ConflictException("SKU já cadastrado para outro produto.");
        }

        product.setName(data.name());
        product.setCategory(data.category());
        product.setBrand(data.brand());
        product.setPrice(data.price());
        if (data.stockQuantity() != null) {
            product.setStockQuantity(data.stockQuantity());
        }
        product.setSku(data.sku());

        repository.save(product);
        return ResponseEntity.ok(new ProductResponseDTO(product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        Barber owner = tenantContext.getCurrentBarber();

        var product = repository.findByIdAndOwnerBarberId(id, owner.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado."));

        repository.delete(product);
        return ResponseEntity.noContent().build();
    }
}