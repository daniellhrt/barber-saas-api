package br.com.daniel.danbarbersaasapi.controllers;

import br.com.daniel.danbarbersaasapi.domain.product.Product;
import br.com.daniel.danbarbersaasapi.domain.product.ProductRequestDTO;
import br.com.daniel.danbarbersaasapi.domain.product.ProductResponseDTO;
import br.com.daniel.danbarbersaasapi.infra.exception.ConflictException;
import br.com.daniel.danbarbersaasapi.infra.exception.ResourceNotFoundException;
import br.com.daniel.danbarbersaasapi.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository repository;

    public ProductController(ProductRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ProductResponseDTO> create(@RequestBody @Valid ProductRequestDTO data, UriComponentsBuilder uriBuilder) {
        if (data.sku() != null && repository.existsBySku(data.sku())) {
            throw new ConflictException("SKU já cadastrado para outro produto.");
        }

        Product product = new Product();
        product.setName(data.name());
        product.setCategory(data.category());
        product.setBrand(data.brand());
        product.setPrice(data.price());

        // Se a quantidade vier nula do front, o Java mantém o = 0 que você colocou na classe
        if (data.stockQuantity() != null) {
            product.setStockQuantity(data.stockQuantity());
        }

        product.setSku(data.sku());

        repository.save(product);

        var uri = uriBuilder.path("/products/{id}").buildAndExpand(product.getId()).toUri();
        return ResponseEntity.created(uri).body(new ProductResponseDTO(product));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> listAll() {
        var products = repository.findAll().stream().map(ProductResponseDTO::new).toList();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getById(@PathVariable UUID id) {
        var product = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado."));

        return ResponseEntity.ok(new ProductResponseDTO(product));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ProductResponseDTO> update(@PathVariable UUID id, @RequestBody @Valid ProductRequestDTO data) {
        var product = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado."));

        if (data.sku() != null && !data.sku().equals(product.getSku()) && repository.existsBySku(data.sku())) {
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
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Produto não encontrado.");
        }

        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}