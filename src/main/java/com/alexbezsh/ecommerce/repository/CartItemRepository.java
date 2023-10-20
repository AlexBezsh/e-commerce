package com.alexbezsh.ecommerce.repository;

import com.alexbezsh.ecommerce.model.db.cartitems.CartItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CartItemRepository extends MongoRepository<CartItem, String> {

    List<CartItem> findByUserId(String userId);

    Optional<CartItem> findByUserIdAndProductId(String userId, String productId);

    void deleteByUserId(String userId);

}
