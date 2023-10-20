package com.alexbezsh.ecommerce.mapper;

import com.alexbezsh.ecommerce.model.api.dto.OrderDto;
import com.alexbezsh.ecommerce.model.db.cartitems.CartItem;
import com.alexbezsh.ecommerce.model.db.orders.Order;
import com.alexbezsh.ecommerce.model.db.orders.OrderItem;
import com.alexbezsh.ecommerce.model.db.products.Product;
import java.math.BigDecimal;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import static com.alexbezsh.ecommerce.model.Calculable.getTotal;

@Mapper(componentModel = "spring")
public abstract class OrderMapper {

    public abstract List<OrderDto> toDtos(List<Order> entities);

    @Mapping(target = "totalPrice", source = "orderItems", qualifiedByName = "getTotalPrice")
    public abstract OrderDto toDto(Order entity);

    @Mapping(target = "name", source = "product.name")
    @Mapping(target = "price", source = "product.price")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "quantity", source = "item.quantity")
    public abstract OrderItem toOrderItem(CartItem item, Product product);

    @Named("getTotalPrice")
    protected BigDecimal getTotalPrice(List<OrderItem> orderItems) {
        return getTotal(orderItems);
    }

}
