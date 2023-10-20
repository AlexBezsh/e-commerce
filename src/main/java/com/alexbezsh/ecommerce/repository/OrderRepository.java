package com.alexbezsh.ecommerce.repository;

import com.alexbezsh.ecommerce.model.db.orders.Order;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<Order, String> {

    List<Order> findByUserId(String userId);

    Optional<Order> findByIdAndUserId(String id, String userId);

}
