package com.alexbezsh.ecommerce.controller;

import com.alexbezsh.ecommerce.controller.api.OrderApi;
import com.alexbezsh.ecommerce.model.api.dto.OrderDto;
import com.alexbezsh.ecommerce.model.api.request.PaymentRequest;
import com.alexbezsh.ecommerce.model.api.response.OrdersResponse;
import com.alexbezsh.ecommerce.model.api.response.PaymentUrlResponse;
import com.alexbezsh.ecommerce.service.OrderService;
import com.alexbezsh.ecommerce.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController implements OrderApi {

    private final OrderService orderService;
    private final PaymentService paymentService;

    @Override
    public OrdersResponse getAllOrders() {
        return orderService.getAll();
    }

    @Override
    public OrderDto createOrder() {
        return orderService.createOrder();
    }

    @Override
    public OrderDto cancelOrder(String id) {
        return orderService.cancelOrder(id);
    }

    @Override
    public PaymentUrlResponse initPayment(String id) {
        return paymentService.initPayment(id);
    }

    @Override
    public void pay(String id, PaymentRequest request) {
        paymentService.pay(id, request);
    }

}
