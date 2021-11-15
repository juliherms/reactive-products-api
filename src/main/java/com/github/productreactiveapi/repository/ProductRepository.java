package com.github.productreactiveapi.repository;

import com.github.productreactiveapi.model.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

/**
 * This class responsible to access Product
 */
public interface ProductRepository
        extends ReactiveMongoRepository<Product, String> {
}
