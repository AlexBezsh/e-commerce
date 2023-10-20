package com.alexbezsh.ecommerce.service;

import com.alexbezsh.ecommerce.exception.ValidationException;
import com.alexbezsh.ecommerce.exception.notfound.OrderNotFoundException;
import com.alexbezsh.ecommerce.mapper.OrderMapper;
import com.alexbezsh.ecommerce.model.OrderStatus;
import com.alexbezsh.ecommerce.model.api.dto.OrderDto;
import com.alexbezsh.ecommerce.model.api.response.OrdersResponse;
import com.alexbezsh.ecommerce.model.db.cartitems.CartItem;
import com.alexbezsh.ecommerce.model.db.orders.Order;
import com.alexbezsh.ecommerce.model.db.orders.OrderHistoryRecord;
import com.alexbezsh.ecommerce.model.db.products.Product;
import com.alexbezsh.ecommerce.repository.OrderRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;
import org.springframework.security.core.context.SecurityContextHolder;
import static com.alexbezsh.ecommerce.TestUtils.CLOCK;
import static com.alexbezsh.ecommerce.TestUtils.ITEM_1_QUANTITY;
import static com.alexbezsh.ecommerce.TestUtils.ITEM_2_QUANTITY;
import static com.alexbezsh.ecommerce.TestUtils.ORDER_ID;
import static com.alexbezsh.ecommerce.TestUtils.USER_ID;
import static com.alexbezsh.ecommerce.TestUtils.cartItem1;
import static com.alexbezsh.ecommerce.TestUtils.cartItem2;
import static com.alexbezsh.ecommerce.TestUtils.cartItemsWithProducts;
import static com.alexbezsh.ecommerce.TestUtils.order;
import static com.alexbezsh.ecommerce.TestUtils.orderDto;
import static com.alexbezsh.ecommerce.TestUtils.orderHistoryRecord;
import static com.alexbezsh.ecommerce.TestUtils.orderItem1;
import static com.alexbezsh.ecommerce.TestUtils.orderItem2;
import static com.alexbezsh.ecommerce.TestUtils.ordersResponse;
import static com.alexbezsh.ecommerce.TestUtils.product1;
import static com.alexbezsh.ecommerce.TestUtils.product2;
import static com.alexbezsh.ecommerce.utils.SecurityUtilsTest.mockUser;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService testedInstance;

    @Spy
    private Clock clock = CLOCK;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private CartService cartService;

    @Mock
    private ProductService productService;

    @Mock
    private OrderRepository orderRepository;

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAll() {
        List<Order> orders = List.of(order());
        OrdersResponse expected = ordersResponse();

        mockUser();
        doReturn(orders).when(orderRepository).findByUserId(USER_ID);
        doReturn(expected.getOrders()).when(orderMapper).toDtos(orders);

        OrdersResponse actual = testedInstance.getAll();

        assertEquals(expected, actual);
    }

    @Test
    void createOrder() {
        List<Pair<CartItem, Product>> cartItemsWithProducts = cartItemsWithProducts();
        Order order = order();
        order.setId(null);
        OrderDto expected = orderDto();

        mockUser();
        doReturn(cartItemsWithProducts).when(cartService).getCartItems(true);
        doReturn(orderItem1()).when(orderMapper).toOrderItem(cartItem1(), product1());
        doReturn(orderItem2()).when(orderMapper).toOrderItem(cartItem2(), product2());
        doReturn(expected).when(orderMapper).toDto(order);

        InOrder inOrder = inOrder(orderRepository, cartService, orderMapper);

        OrderDto actual = testedInstance.createOrder();

        assertEquals(expected, actual);

        inOrder.verify(orderRepository).save(order);
        inOrder.verify(cartService).deleteAll();
        inOrder.verify(orderMapper).toDto(order);
    }

    @Test
    void createOrderShouldThrowValidationException() {
        doReturn(emptyList()).when(cartService).getCartItems(true);

        ValidationException e = assertThrows(ValidationException.class,
            () -> testedInstance.createOrder());

        assertEquals("Add items to cart before placing the order", e.getMessage());
    }

    @Test
    void cancelOrder() {
        Order order = order();
        Order cancelledOrder = order();
        cancelledOrder.setStatus(OrderStatus.CANCELLED);
        cancelledOrder.getHistory().add(OrderHistoryRecord.builder()
            .status(OrderStatus.CANCELLED)
            .dateTime(LocalDateTime.now(CLOCK))
            .build());
        Product product1 = product1();
        product1.setStockQuantity(product1.getStockQuantity() + ITEM_1_QUANTITY);
        Product product2 = product2();
        product2.setStockQuantity(product2.getStockQuantity() + ITEM_2_QUANTITY);
        OrderDto expected = orderDto();

        mockUser();
        doReturn(Optional.of(order)).when(orderRepository).findByIdAndUserId(ORDER_ID, USER_ID);
        doReturn(cancelledOrder).when(orderRepository).save(cancelledOrder);
        doReturn(expected).when(orderMapper).toDto(order);

        OrderDto actual = testedInstance.cancelOrder(ORDER_ID);

        assertEquals(expected, actual);

        verify(productService).addOrderedItemsToStock(order.getOrderItems());
    }

    @Test
    void cancelOrderShouldThrowNotFoundException() {
        mockUser();
        doReturn(Optional.empty()).when(orderRepository).findByIdAndUserId(ORDER_ID, USER_ID);

        assertThrows(OrderNotFoundException.class, () -> testedInstance.cancelOrder(ORDER_ID));

        verify(orderRepository, never()).save(any());
        verify(productService, never()).addOrderedItemsToStock(any());
    }

    @Test
    void cancelOrderShouldThrowValidationException() {
        Order order = order();
        order.setStatus(OrderStatus.PAID);

        mockUser();
        doReturn(Optional.of(order)).when(orderRepository).findByIdAndUserId(ORDER_ID, USER_ID);

        assertThrows(ValidationException.class, () -> testedInstance.cancelOrder(ORDER_ID));

        verify(orderRepository, never()).save(any());
        verify(productService, never()).addOrderedItemsToStock(any());
    }

    @Test
    void updateStatusToNew() {
        Order order = new Order();
        Order expected = Order.builder()
            .status(OrderStatus.NEW)
            .dateTime(LocalDateTime.now(clock))
            .history(List.of(orderHistoryRecord()))
            .build();

        testedInstance.saveNewStatus(order, OrderStatus.NEW);

        verify(orderRepository).save(expected);
    }

    @Test
    void updateStatusToCancelled() {
        Order order = new Order();
        order.setHistory(new ArrayList<>(List.of(orderHistoryRecord())));
        Order expected = Order.builder()
            .status(OrderStatus.CANCELLED)
            .history(List.of(
                orderHistoryRecord(),
                OrderHistoryRecord.builder()
                    .status(OrderStatus.CANCELLED)
                    .dateTime(LocalDateTime.now(clock))
                    .build()))
            .build();

        testedInstance.saveNewStatus(order, OrderStatus.CANCELLED);

        verify(orderRepository).save(expected);
    }

}
