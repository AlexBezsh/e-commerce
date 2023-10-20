package com.alexbezsh.ecommerce.service;

import com.alexbezsh.ecommerce.exception.PayPalException;
import com.alexbezsh.ecommerce.exception.ValidationException;
import com.alexbezsh.ecommerce.model.OrderStatus;
import com.alexbezsh.ecommerce.model.api.request.PaymentRequest;
import com.alexbezsh.ecommerce.model.api.response.PaymentUrlResponse;
import com.alexbezsh.ecommerce.model.db.orders.Order;
import com.alexbezsh.ecommerce.properties.PayPalProperties;
import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.PayerInfo;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.RedirectUrls;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.alexbezsh.ecommerce.model.Calculable.getTotal;
import static com.alexbezsh.ecommerce.utils.SecurityUtils.getUserEmail;
import static com.alexbezsh.ecommerce.utils.SecurityUtils.getUserId;

@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(PayPalProperties.class)
public class PaymentService {

    private final APIContext apiContext;
    private final OrderService orderService;
    private final PayPalProperties payPalProperties;

    public PaymentUrlResponse initPayment(String orderId) {
        Order order = orderService.getOrder(getUserId(), orderId);
        validateStatus(order);
        Payment paymentRequest = getPaymentRequest(order);
        return PaymentUrlResponse.builder()
            .paymentUrl(getPaymentUrl(paymentRequest))
            .build();
    }

    @Transactional
    public void pay(String orderId, PaymentRequest request) {
        Order order = orderService.getOrder(getUserId(), orderId);
        validateStatus(order);
        orderService.saveNewStatus(order, OrderStatus.PAID);
        PaymentExecution paymentExecution = new PaymentExecution().setPayerId(request.getPayerId());
        Payment payment = new Payment().setId(request.getPaymentId());
        executePayment(paymentExecution, payment);
    }

    protected Payment getPaymentRequest(Order order) {
        PayerInfo payerInfo = new PayerInfo().setEmail(getUserEmail());
        Payer payer = new Payer()
            .setPaymentMethod("paypal")
            .setPayerInfo(payerInfo);

        BigDecimal total = getTotal(order.getOrderItems());
        Transaction transaction = new Transaction();
        transaction.setAmount(new Amount()
            .setCurrency("USD")
            .setTotal(total.toString()));

        RedirectUrls redirectUrls = new RedirectUrls()
            .setCancelUrl(String.format(payPalProperties.getCancelUrl(), order.getId()))
            .setReturnUrl(String.format(payPalProperties.getReturnUrl(), order.getId()));

        return new Payment()
            .setIntent("sale")
            .setPayer(payer)
            .setTransactions(List.of(transaction))
            .setRedirectUrls(redirectUrls);
    }

    protected String getPaymentUrl(Payment paymentRequest) {
        try {
            Payment paymentResponse = paymentRequest.create(apiContext);
            return paymentResponse.getLinks()
                .stream()
                .filter(link -> "approval_url".equals(link.getRel()))
                .findFirst()
                .map(Links::getHref)
                .orElseThrow(() -> new RuntimeException("No approval URL in PayPal response"));
        } catch (PayPalRESTException e) {
            throw new PayPalException(e);
        }
    }

    protected void executePayment(PaymentExecution execution, Payment payment) {
        try {
            // user enters PII information in PayPal when visits approval_url
            // we can take shipping address and user info from result below if needed
            // Payment result = payment.execute(apiContext, execution);
            payment.execute(apiContext, execution);
        } catch (PayPalRESTException e) {
            throw new PayPalException(e);
        }
    }

    private void validateStatus(Order order) {
        if (order.getStatus() != OrderStatus.NEW) {
            throw new ValidationException("Only new order can be paid");
        }
    }

}
