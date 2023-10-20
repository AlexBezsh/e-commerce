package com.alexbezsh.ecommerce.model.api.response;

import com.alexbezsh.ecommerce.model.api.dto.OrderDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdersResponse {

    private List<OrderDto> orders;

}
