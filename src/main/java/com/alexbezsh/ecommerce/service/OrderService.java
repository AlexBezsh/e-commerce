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
import com.alexbezsh.ecommerce.model.db.orders.OrderItem;
import com.alexbezsh.ecommerce.model.db.products.Product;
import com.alexbezsh.ecommerce.repository.OrderRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.alexbezsh.ecommerce.model.OrderStatus.CANCELLED;
import static com.alexbezsh.ecommerce.model.OrderStatus.NEW;
import static com.alexbezsh.ecommerce.utils.SecurityUtils.getUserId;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final Clock clock;
    private final OrderMapper orderMapper;
    private final CartService cartService;
    private final ProductService productService;
    private final OrderRepository orderRepository;

    public OrdersResponse getAll() {
        List<Order> orders = orderRepository.findByUserId(getUserId());
        return OrdersResponse.builder()
            .orders(orderMapper.toDtos(orders))
            .build();
    }

    @Transactional
    public OrderDto createOrder() {
        List<OrderItem> orderItems = retrieveCartItems().stream()
            .map(pair -> orderMapper.toOrderItem(pair.getFirst(), pair.getSecond()))
            .toList();
        Order order = Order.builder()
            .userId(getUserId())
            .orderItems(orderItems)
            .build();
        saveNewStatus(order, NEW);
        cartService.deleteAll();
        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDto cancelOrder(String id) {
        Order order = getOrder(getUserId(), id);
        if (order.getStatus() != NEW) {
            throw new ValidationException("Only new order can be canceled");
        }
        saveNewStatus(order, CANCELLED);
        productService.addOrderedItemsToStock(order.getOrderItems());
        return orderMapper.toDto(order);
    }

    public void saveNewStatus(Order order, OrderStatus status) {
        order.setStatus(status);
        LocalDateTime dateTime = LocalDateTime.now(clock);
        if (status == NEW) {
            order.setDateTime(dateTime);
        }
        if (order.getHistory() == null) {
            order.setHistory(new ArrayList<>());
        }
        order.getHistory().add(OrderHistoryRecord.builder()
            .status(status)
            .dateTime(dateTime)
            .build());
        orderRepository.save(order);
    }

    private List<Pair<CartItem, Product>> retrieveCartItems() {
        List<Pair<CartItem, Product>> cart = cartService.getCartItems(true);
        if (cart.isEmpty()) {
            throw new ValidationException("Add items to cart before placing the order");
        }
        return cart;
    }

    public Order getOrder(String userId, String id) {
        return orderRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new OrderNotFoundException(id));
    }

}
