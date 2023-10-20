package com.alexbezsh.ecommerce.model.db.orders;

import com.alexbezsh.ecommerce.model.OrderStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderHistoryRecord {

    private OrderStatus status;
    private LocalDateTime dateTime;

}
