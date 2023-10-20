package com.alexbezsh.ecommerce.mapper;

import com.alexbezsh.ecommerce.model.api.dto.OrderDto;
import com.alexbezsh.ecommerce.model.db.cartitems.CartItem;
import com.alexbezsh.ecommerce.model.db.orders.Order;
import com.alexbezsh.ecommerce.model.db.orders.OrderItem;
import com.alexbezsh.ecommerce.model.db.products.Product;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import static com.alexbezsh.ecommerce.TestUtils.cartItem1;
import static com.alexbezsh.ecommerce.TestUtils.order;
import static com.alexbezsh.ecommerce.TestUtils.orderDto;
import static com.alexbezsh.ecommerce.TestUtils.orderItem1;
import static com.alexbezsh.ecommerce.TestUtils.orderItem2;
import static com.alexbezsh.ecommerce.TestUtils.product1;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OrderMapperTest {

    private final OrderMapper testedInstance = Mappers.getMapper(OrderMapper.class);

    @Test
    void toOrderDtos() {
        List<Order> entities = List.of(order());
        List<OrderDto> expected = List.of(orderDto());

        List<OrderDto> actual = testedInstance.toDtos(entities);

        assertEquals(expected, actual);
    }

    @Test
    void toOrderDtosShouldReturnNull() {
        assertNull(testedInstance.toDtos(null));
    }

    @Test
    void toOrderDtosShouldReturnEmtyList() {
        assertEquals(emptyList(), testedInstance.toDtos(emptyList()));
    }

    @Test
    void toOrderDto() {
        Order entity = order();
        OrderDto expected = orderDto();

        OrderDto actual = testedInstance.toDto(entity);

        assertEquals(expected, actual);
    }

    @Test
    void toOrderDtoShouldReturnNull() {
        assertNull(testedInstance.toDto((Order) null));
    }

    @Test
    void toOrderItem() {
        Product product = product1();
        CartItem cartItem = cartItem1();
        OrderItem expected = orderItem1();

        OrderItem actual = testedInstance.toOrderItem(cartItem, product);

        assertEquals(expected, actual);
    }

    @Test
    void toOrderItemShouldReturnNull() {
        assertNull(testedInstance.toOrderItem(null, null));
    }

    @ParameterizedTest
    @MethodSource("totalPriceArgs")
    void getTotalPrice(List<OrderItem> entities, BigDecimal expected) {
        assertEquals(expected, testedInstance.getTotalPrice(entities));
    }

    static Stream<Arguments> totalPriceArgs() {
        return Stream.of(
            Arguments.of(List.of(orderItem1()), new BigDecimal("3.0")),
            Arguments.of(List.of(orderItem2()), new BigDecimal("2.5")),
            Arguments.of(List.of(orderItem1(), orderItem2()), new BigDecimal("5.5"))
        );
    }

}
