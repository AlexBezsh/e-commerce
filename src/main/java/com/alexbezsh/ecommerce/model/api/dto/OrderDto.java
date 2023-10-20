package com.alexbezsh.ecommerce.model.api.dto;

import com.alexbezsh.ecommerce.model.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

    private String id;
    private String userId;
    private OrderStatus status;
    private BigDecimal totalPrice;
    private LocalDateTime dateTime;
    private List<OrderItemDto> orderItems;
    private List<OrderHistoryRecordDto> history;

}
