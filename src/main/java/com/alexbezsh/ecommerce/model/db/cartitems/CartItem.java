package com.alexbezsh.ecommerce.model.db.cartitems;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("cart.items")
public class CartItem {

    @Id
    private String id;
    @Indexed
    private String userId;
    @Indexed
    private String productId;
    private int quantity;

}
