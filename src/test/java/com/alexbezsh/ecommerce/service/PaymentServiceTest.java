package com.alexbezsh.ecommerce.service;

import com.alexbezsh.ecommerce.exception.PayPalException;
import com.alexbezsh.ecommerce.exception.ValidationException;
import com.alexbezsh.ecommerce.model.OrderStatus;
import com.alexbezsh.ecommerce.model.api.request.PaymentRequest;
import com.alexbezsh.ecommerce.model.api.response.PaymentUrlResponse;
import com.alexbezsh.ecommerce.model.db.orders.Order;
import com.alexbezsh.ecommerce.properties.PayPalProperties;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import static com.alexbezsh.ecommerce.TestUtils.ORDER_ID;
import static com.alexbezsh.ecommerce.TestUtils.PAYER_ID;
import static com.alexbezsh.ecommerce.TestUtils.PAYMENT_ID;
import static com.alexbezsh.ecommerce.TestUtils.PAYMENT_LINK;
import static com.alexbezsh.ecommerce.TestUtils.USER_ID;
import static com.alexbezsh.ecommerce.TestUtils.order;
import static com.alexbezsh.ecommerce.TestUtils.paymentRequest;
import static com.alexbezsh.ecommerce.TestUtils.paymentUrlResponse;
import static com.alexbezsh.ecommerce.utils.SecurityUtilsTest.mockUser;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private static final String CANCEL_URL = "http://cancel.url";
    private static final String RETURN_URL = "http://return.url";

    @Spy
    @InjectMocks
    private PaymentService testedInstance;

    @Mock
    private APIContext apiContext;

    @Mock
    private OrderService orderService;

    @Mock
    private PayPalProperties payPalProperties;

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void initPayment() throws PayPalRESTException {
        Order order = order();
        Payment paymentRequest = mock(Payment.class);
        Payment paymentResponse = new Payment()
            .setLinks(List.of(
                new Links()
                    .setRel("approval_url")
                    .setHref(PAYMENT_LINK)));
        PaymentUrlResponse expected = paymentUrlResponse();

        mockUser();
        doReturn(order).when(orderService).getOrder(USER_ID, ORDER_ID);
        doReturn(paymentRequest).when(testedInstance).getPaymentRequest(order);
        doReturn(paymentResponse).when(paymentRequest).create(apiContext);

        PaymentUrlResponse actual = testedInstance.initPayment(ORDER_ID);

        assertEquals(expected, actual);
    }

    @Test
    void initPaymentShouldThrowValidationException() {
        Order order = order();
        order.setStatus(OrderStatus.CANCELLED);

        mockUser();
        doReturn(order).when(orderService).getOrder(USER_ID, ORDER_ID);

        assertThrows(ValidationException.class,
            () -> testedInstance.initPayment(ORDER_ID));
    }

    @Test
    void initPaymentShouldThrowExceptionIfPayPalRequestFails() throws PayPalRESTException {
        Order order = order();
        Payment paymentRequest = mock(Payment.class);

        mockUser();
        doReturn(order).when(orderService).getOrder(USER_ID, ORDER_ID);
        doReturn(paymentRequest).when(testedInstance).getPaymentRequest(order);
        doThrow(PayPalRESTException.class).when(paymentRequest).create(apiContext);

        assertThrows(PayPalException.class, () -> testedInstance.initPayment(ORDER_ID));
    }

    @Test
    void initPaymentShouldThrowExceptionIfNoPayPalLink() throws PayPalRESTException {
        Order order = order();
        Payment paymentRequest = mock(Payment.class);
        Payment paymentResponse = new Payment().setLinks(emptyList());

        mockUser();
        doReturn(order).when(orderService).getOrder(USER_ID, ORDER_ID);
        doReturn(paymentRequest).when(testedInstance).getPaymentRequest(order);
        doReturn(paymentResponse).when(paymentRequest).create(apiContext);

        assertThrows(RuntimeException.class, () -> testedInstance.initPayment(ORDER_ID));
    }

    @Test
    void pay() {
        PaymentRequest request = paymentRequest();
        Order order = order();
        PaymentExecution paymentExecution = new PaymentExecution().setPayerId(PAYER_ID);
        Payment payment = new Payment().setId(PAYMENT_ID);

        mockUser();
        doReturn(order).when(orderService).getOrder(USER_ID, ORDER_ID);
        doNothing().when(testedInstance).executePayment(paymentExecution, payment);

        testedInstance.pay(ORDER_ID, request);

        verify(orderService).saveNewStatus(order, OrderStatus.PAID);
        verify(testedInstance).executePayment(paymentExecution, payment);
    }

    @Test
    void payShouldThrowValidationException() {
        PaymentRequest request = paymentRequest();
        Order order = order();
        order.setStatus(OrderStatus.CANCELLED);

        mockUser();
        doReturn(order).when(orderService).getOrder(USER_ID, ORDER_ID);

        assertThrows(ValidationException.class,
            () -> testedInstance.pay(ORDER_ID, request));

        verify(testedInstance, never()).executePayment(any(), any());
    }

    @Test
    void getPaymentRequest() {
        Order order = order();
        String expected = """
            {
              "intent": "sale",
              "payer": {
                "payment_method": "paypal",
                "payer_info": {
                  "email": "some@email.com"
                }
              },
              "transactions": [
                {
                  "amount": {
                    "currency": "USD",
                    "total": "5.5"
                  }
                }
              ],
              "redirect_urls": {
                "return_url": "http://return.url",
                "cancel_url": "http://cancel.url"
              }
            }""";

        mockUser();
        doReturn(CANCEL_URL).when(payPalProperties).getCancelUrl();
        doReturn(RETURN_URL).when(payPalProperties).getReturnUrl();

        Payment actual = testedInstance.getPaymentRequest(order);

        assertEquals(expected, actual.toJSON());
    }

    @Test
    void executePayment() throws PayPalRESTException {
        PaymentExecution paymentExecution = mock(PaymentExecution.class);
        Payment payment = mock(Payment.class);

        testedInstance.executePayment(paymentExecution, payment);

        verify(payment).execute(apiContext, paymentExecution);
    }

    @Test
    void executePaymentShouldThrowRuntimeException() throws PayPalRESTException {
        PaymentExecution paymentExecution = mock(PaymentExecution.class);
        Payment payment = mock(Payment.class);

        doThrow(PayPalRESTException.class).when(payment).execute(apiContext, paymentExecution);

        assertThrows(RuntimeException.class,
            () -> testedInstance.executePayment(paymentExecution, payment));
    }

}
