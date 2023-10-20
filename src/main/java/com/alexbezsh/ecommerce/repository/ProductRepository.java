package com.alexbezsh.ecommerce.repository;

import com.alexbezsh.ecommerce.model.db.products.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String> {
}
