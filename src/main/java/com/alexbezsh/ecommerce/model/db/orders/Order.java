package com.alexbezsh.ecommerce.model.db.orders;

import com.alexbezsh.ecommerce.model.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;
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
@Document("orders")
public class Order {

    @Id
    private String id;
    @Indexed
    private String userId;
    private OrderStatus status;
    private LocalDateTime dateTime;
    private List<OrderItem> orderItems;
    private List<OrderHistoryRecord> history;

}
