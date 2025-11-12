package com.promopricer.cart.pricer.controller;

import com.promopricer.cart.pricer.controller.domains.ProductRequest;
import com.promopricer.cart.pricer.models.Product;
import com.promopricer.cart.pricer.models.ProductCategory;
import com.promopricer.cart.pricer.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
class ProductController {
    private final ProductRepository productRepository;
    public ProductController(ProductRepository productRepository) { this.productRepository = productRepository; }

    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody ProductRequest request) {

        Product product = new Product();
        product.setId(request.id());
        product.setName(request.name());
        product.setCategory(ProductCategory.valueOf(request.category()));
        product.setPrice(request.price());
        product.setStock(request.stock());
        Product saved = productRepository.save(product);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
