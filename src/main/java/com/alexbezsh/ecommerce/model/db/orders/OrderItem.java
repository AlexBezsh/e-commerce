package com.alexbezsh.ecommerce.model.db.orders;

import com.alexbezsh.ecommerce.model.Calculable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem implements Calculable {

    private String name;
    private int quantity;
    private BigDecimal price;
    private String productId;

}
