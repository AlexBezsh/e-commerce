package com.alexbezsh.ecommerce.controller;

import com.alexbezsh.ecommerce.repository.CartItemRepository;
import com.alexbezsh.ecommerce.repository.OrderRepository;
import com.alexbezsh.ecommerce.repository.ProductRepository;
import com.alexbezsh.ecommerce.service.CartService;
import com.alexbezsh.ecommerce.service.OrderService;
import com.alexbezsh.ecommerce.service.PaymentService;
import com.alexbezsh.ecommerce.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    protected CartService cartService;

    @MockBean
    protected OrderService orderService;

    @MockBean
    protected ProductService productService;

    @MockBean
    protected PaymentService paymentService;

    @MockBean
    protected OrderRepository orderRepository;

    @MockBean
    protected ProductRepository productRepository;

    @MockBean
    protected CartItemRepository cartItemRepository;

}
