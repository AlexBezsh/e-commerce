package com.alexbezsh.ecommerce.controller;

import com.alexbezsh.ecommerce.model.api.dto.OrderDto;
import com.alexbezsh.ecommerce.model.api.request.PaymentRequest;
import com.alexbezsh.ecommerce.model.api.response.OrdersResponse;
import com.alexbezsh.ecommerce.model.api.response.PaymentUrlResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import static com.alexbezsh.ecommerce.TestRole.TEST;
import static com.alexbezsh.ecommerce.TestRole.USER;
import static com.alexbezsh.ecommerce.TestUtils.ORDER_ID;
import static com.alexbezsh.ecommerce.TestUtils.forbiddenResponse;
import static com.alexbezsh.ecommerce.TestUtils.orderDto;
import static com.alexbezsh.ecommerce.TestUtils.orderNotFoundException;
import static com.alexbezsh.ecommerce.TestUtils.orderNotFoundResponse;
import static com.alexbezsh.ecommerce.TestUtils.ordersResponse;
import static com.alexbezsh.ecommerce.TestUtils.payPalException;
import static com.alexbezsh.ecommerce.TestUtils.payPalExceptionResponse;
import static com.alexbezsh.ecommerce.TestUtils.paymentRequest;
import static com.alexbezsh.ecommerce.TestUtils.paymentUrlResponse;
import static com.alexbezsh.ecommerce.TestUtils.toJson;
import static com.alexbezsh.ecommerce.TestUtils.unauthorizedResponse;
import static com.alexbezsh.ecommerce.TestUtils.unexpectedErrorResponse;
import static com.alexbezsh.ecommerce.TestUtils.validationException;
import static com.alexbezsh.ecommerce.TestUtils.validationResponse;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerTest extends AbstractControllerTest {

    private static final String BASE_URL = "/api/v1/orders";
    private static final String ORDER_URL = BASE_URL + "/" + ORDER_ID;
    private static final String CANCEL_ORDER_URL = ORDER_URL + "/cancel";
    private static final String INIT_PAYMENT_URL = ORDER_URL + "/payment";
    private static final String PAY_ORDER_URL = ORDER_URL + "/pay";

    @Test
    public void getAllOrders() throws Exception {
        OrdersResponse expected = ordersResponse();

        doReturn(expected).when(orderService).getAll();

        mockMvc.perform(get(BASE_URL)
                .with(jwt().authorities(USER)))
            .andExpect(status().isOk())
            .andExpect(content().json(toJson(expected)));
    }

    @Test
    public void getAllOrdersShouldReturn401() throws Exception {
        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isUnauthorized())
            .andExpect(content().json(unauthorizedResponse()));
    }

    @Test
    public void getAllOrdersShouldReturn403() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .with(jwt().authorities(TEST)))
            .andExpect(status().isForbidden())
            .andExpect(content().json(forbiddenResponse()));
    }

    @Test
    public void getAllOrdersShouldReturn500() throws Exception {
        doThrow(RuntimeException.class).when(orderService).getAll();

        mockMvc.perform(get(BASE_URL)
                .with(jwt().authorities(USER)))
            .andExpect(status().isInternalServerError())
            .andExpect(content().json(unexpectedErrorResponse()));
    }

    @Test
    public void createOrder() throws Exception {
        OrderDto expected = orderDto();

        doReturn(expected).when(orderService).createOrder();

        mockMvc.perform(post(BASE_URL)
                .with(jwt().authorities(USER)))
            .andExpect(status().isCreated())
            .andExpect(content().json(toJson(expected)));
    }

    @Test
    public void createOrderShouldReturn400() throws Exception {
        doThrow(validationException()).when(orderService).createOrder();

        mockMvc.perform(post(BASE_URL)
                .with(jwt().authorities(USER)))
            .andExpect(status().isBadRequest())
            .andExpect(content().json(validationResponse()));
    }

    @Test
    public void createOrderShouldReturn401() throws Exception {
        mockMvc.perform(post(BASE_URL).with(csrf()))
            .andExpect(status().isUnauthorized())
            .andExpect(content().json(unauthorizedResponse()));
    }

    @Test
    public void createOrderShouldReturn403() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .with(jwt().authorities(TEST)))
            .andExpect(status().isForbidden())
            .andExpect(content().json(forbiddenResponse()));
    }

    @Test
    public void createOrderShouldReturn500() throws Exception {
        doThrow(RuntimeException.class).when(orderService).createOrder();

        mockMvc.perform(post(BASE_URL)
                .with(jwt().authorities(USER)))
            .andExpect(status().isInternalServerError())
            .andExpect(content().json(unexpectedErrorResponse()));
    }

    @Test
    public void cancelOrder() throws Exception {
        OrderDto expected = orderDto();

        doReturn(expected).when(orderService).cancelOrder(ORDER_ID);

        mockMvc.perform(patch(CANCEL_ORDER_URL)
                .with(jwt().authorities(USER)))
            .andExpect(status().isOk())
            .andExpect(content().json(toJson(expected)));
    }

    @Test
    public void cancelOrderShouldReturn400() throws Exception {
        doThrow(validationException()).when(orderService).cancelOrder(ORDER_ID);

        mockMvc.perform(patch(CANCEL_ORDER_URL)
                .with(jwt().authorities(USER)))
            .andExpect(status().isBadRequest())
            .andExpect(content().json(validationResponse()));
    }

    @Test
    public void cancelOrderShouldReturn401() throws Exception {
        mockMvc.perform(patch(CANCEL_ORDER_URL).with(csrf()))
            .andExpect(status().isUnauthorized())
            .andExpect(content().json(unauthorizedResponse()));
    }

    @Test
    public void cancelOrderShouldReturn403() throws Exception {
        mockMvc.perform(patch(CANCEL_ORDER_URL)
                .with(jwt().authorities(TEST)))
            .andExpect(status().isForbidden())
            .andExpect(content().json(forbiddenResponse()));
    }

    @Test
    public void cancelOrderShouldReturn404() throws Exception {
        doThrow(orderNotFoundException()).when(orderService).cancelOrder(ORDER_ID);

        mockMvc.perform(patch(CANCEL_ORDER_URL)
                .with(jwt().authorities(USER)))
            .andExpect(status().isNotFound())
            .andExpect(content().json(orderNotFoundResponse()));
    }

    @Test
    public void cancelOrderShouldReturn500() throws Exception {
        doThrow(RuntimeException.class).when(orderService).cancelOrder(ORDER_ID);

        mockMvc.perform(patch(CANCEL_ORDER_URL)
                .with(jwt().authorities(USER)))
            .andExpect(status().isInternalServerError())
            .andExpect(content().json(unexpectedErrorResponse()));
    }

    @Test
    public void initPayment() throws Exception {
        PaymentUrlResponse expected = paymentUrlResponse();

        doReturn(expected).when(paymentService).initPayment(ORDER_ID);

        mockMvc.perform(post(INIT_PAYMENT_URL)
                .with(jwt().authorities(USER)))
            .andExpect(status().isOk())
            .andExpect(content().json(toJson(expected)));
    }

    @Test
    public void initPaymentShouldReturn400() throws Exception {
        doThrow(validationException()).when(paymentService).initPayment(ORDER_ID);

        mockMvc.perform(post(INIT_PAYMENT_URL)
                .with(jwt().authorities(USER)))
            .andExpect(status().isBadRequest())
            .andExpect(content().json(validationResponse()));
    }

    @Test
    public void initPaymentShouldHandlePayPalException() throws Exception {
        doThrow(payPalException()).when(paymentService).initPayment(ORDER_ID);

        mockMvc.perform(post(INIT_PAYMENT_URL)
                .with(jwt().authorities(USER)))
            .andExpect(status().isBadRequest())
            .andExpect(content().json(payPalExceptionResponse()));
    }

    @Test
    public void initPaymentShouldReturn401() throws Exception {
        mockMvc.perform(post(INIT_PAYMENT_URL).with(csrf()))
            .andExpect(status().isUnauthorized())
            .andExpect(content().json(unauthorizedResponse()));
    }

    @Test
    public void initPaymentShouldReturn403() throws Exception {
        mockMvc.perform(post(INIT_PAYMENT_URL)
                .with(jwt().authorities(TEST)))
            .andExpect(status().isForbidden())
            .andExpect(content().json(forbiddenResponse()));
    }

    @Test
    public void initPaymentShouldReturn404() throws Exception {
        doThrow(orderNotFoundException()).when(paymentService).initPayment(ORDER_ID);

        mockMvc.perform(post(INIT_PAYMENT_URL)
                .with(jwt().authorities(USER)))
            .andExpect(status().isNotFound())
            .andExpect(content().json(orderNotFoundResponse()));
    }

    @Test
    public void initPaymentShouldReturn500() throws Exception {
        doThrow(RuntimeException.class).when(paymentService).initPayment(ORDER_ID);

        mockMvc.perform(post(INIT_PAYMENT_URL)
                .with(jwt().authorities(USER)))
            .andExpect(status().isInternalServerError())
            .andExpect(content().json(unexpectedErrorResponse()));
    }

    @Test
    public void pay() throws Exception {
        PaymentRequest request = paymentRequest();

        mockMvc.perform(post(PAY_ORDER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request))
                .with(jwt().authorities(USER)))
            .andExpect(status().isNoContent());

        verify(paymentService).pay(ORDER_ID, request);
    }

    @Test
    public void payShouldReturn400() throws Exception {
        PaymentRequest request = paymentRequest();

        doThrow(validationException()).when(paymentService).pay(ORDER_ID, request);

        mockMvc.perform(post(PAY_ORDER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request))
                .with(jwt().authorities(USER)))
            .andExpect(status().isBadRequest())
            .andExpect(content().json(validationResponse()));
    }

    @Test
    public void payShouldHandlePayPalException() throws Exception {
        PaymentRequest request = paymentRequest();

        doThrow(payPalException()).when(paymentService).pay(ORDER_ID, request);

        mockMvc.perform(post(PAY_ORDER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request))
                .with(jwt().authorities(USER)))
            .andExpect(status().isBadRequest())
            .andExpect(content().json(payPalExceptionResponse()));
    }

    @Test
    public void payShouldReturn401() throws Exception {
        mockMvc.perform(post(PAY_ORDER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(paymentRequest()))
                .with(csrf()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void payShouldReturn403() throws Exception {
        mockMvc.perform(post(PAY_ORDER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(paymentRequest()))
                .with(jwt().authorities(TEST)))
            .andExpect(status().isForbidden());
    }

    @Test
    public void payShouldReturn404() throws Exception {
        PaymentRequest request = paymentRequest();

        doThrow(orderNotFoundException()).when(paymentService).pay(ORDER_ID, request);

        mockMvc.perform(post(PAY_ORDER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request))
                .with(jwt().authorities(USER)))
            .andExpect(status().isNotFound())
            .andExpect(content().json(orderNotFoundResponse()));
    }

    @Test
    public void payShouldReturn500() throws Exception {
        PaymentRequest request = paymentRequest();

        doThrow(RuntimeException.class).when(paymentService).pay(ORDER_ID, request);

        mockMvc.perform(post(PAY_ORDER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request))
                .with(jwt().authorities(USER)))
            .andExpect(status().isInternalServerError())
            .andExpect(content().json(unexpectedErrorResponse()));
    }

}
