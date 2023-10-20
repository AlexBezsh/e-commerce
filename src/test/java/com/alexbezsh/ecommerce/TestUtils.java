package com.alexbezsh.ecommerce;

import com.alexbezsh.ecommerce.exception.PayPalException;
import com.alexbezsh.ecommerce.exception.ValidationException;
import com.alexbezsh.ecommerce.exception.notfound.OrderNotFoundException;
import com.alexbezsh.ecommerce.exception.notfound.ProductNotFoundException;
import com.alexbezsh.ecommerce.model.OrderStatus;
import com.alexbezsh.ecommerce.model.api.dto.CartDto;
import com.alexbezsh.ecommerce.model.api.dto.CartItemDto;
import com.alexbezsh.ecommerce.model.api.dto.OrderDto;
import com.alexbezsh.ecommerce.model.api.dto.OrderHistoryRecordDto;
import com.alexbezsh.ecommerce.model.api.dto.OrderItemDto;
import com.alexbezsh.ecommerce.model.api.dto.ProductDto;
import com.alexbezsh.ecommerce.model.api.request.AddCartItemRequest;
import com.alexbezsh.ecommerce.model.api.request.PaymentRequest;
import com.alexbezsh.ecommerce.model.api.response.ErrorResponse;
import com.alexbezsh.ecommerce.model.api.response.OrdersResponse;
import com.alexbezsh.ecommerce.model.api.response.PaymentUrlResponse;
import com.alexbezsh.ecommerce.model.api.response.ProductsResponse;
import com.alexbezsh.ecommerce.model.db.cartitems.CartItem;
import com.alexbezsh.ecommerce.model.db.orders.Order;
import com.alexbezsh.ecommerce.model.db.orders.OrderHistoryRecord;
import com.alexbezsh.ecommerce.model.db.orders.OrderItem;
import com.alexbezsh.ecommerce.model.db.products.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypal.base.rest.PayPalRESTException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.springframework.data.util.Pair;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@UtilityClass
public class TestUtils {

    public static final String USER_ID = "1";
    public static final String USER_EMAIL = "some@email.com";

    public static final String CART_ITEM_1_ID = "2";
    public static final int ITEM_1_QUANTITY = 2;

    public static final String CART_ITEM_2_ID = "3";
    public static final int ITEM_2_QUANTITY = 1;

    public static final String PRODUCT_1_ID = "4";
    public static final String PRODUCT_1_NAME = "product1Name";
    public static final BigDecimal PRODUCT_1_PRICE = new BigDecimal("1.5");
    public static final String PRODUCT_1_DESCRIPTION = "product1Description";
    public static final int PRODUCT_1_STOCK_QUANTITY = 20;

    public static final String PRODUCT_2_ID = "5";
    public static final String PRODUCT_2_NAME = "product2Name";
    public static final BigDecimal PRODUCT_2_PRICE = new BigDecimal("2.5");
    public static final String PRODUCT_2_DESCRIPTION = "product2Description";
    public static final int PRODUCT_2_STOCK_QUANTITY = 10;

    public static final String ORDER_ID = "6";
    public static final BigDecimal TOTAL_PRICE = new BigDecimal("5.5");

    public static final String PAYER_ID = "payerID";
    public static final String PAYMENT_ID = "paymentID";
    public static final String PAYMENT_LINK = "https://payment.link";

    public static final String FORBIDDEN_ERROR = "Access denied";
    public static final String VALIDATION_MESSAGE = "test validation message";
    public static final String PAY_PAL_ERROR_MESSAGE = "PayPal error message";
    public static final String UNEXPECTED_ERROR = "Unexpected error. Reason: null";
    public static final String ORDER_NOT_FOUND_MESSAGE = "Order " + ORDER_ID + " not found";
    public static final String PRODUCT_NOT_FOUND_MESSAGE = "Product " + PRODUCT_1_ID + " not found";
    public static final String UNAUTHORIZED_ERROR =
        "Full authentication is required to access this resource";

    public static final Clock CLOCK = Clock.fixed(
        Instant.parse("2023-12-03T10:15:30.00Z"), ZoneId.systemDefault());

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .findAndRegisterModules()
        .disable(WRITE_DATES_AS_TIMESTAMPS);

    public static String toJson(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(
                "Unable to convert object to JSON. Reason: " + e.getMessage());
        }
    }

    public static CartDto cartDto() {
        return CartDto.builder()
            .totalPrice(TOTAL_PRICE)
            .cartItems(List.of(cartItemDto1(), cartItemDto2()))
            .build();
    }

    public static CartItemDto cartItemDto1() {
        return CartItemDto.builder()
            .id(CART_ITEM_1_ID)
            .productId(PRODUCT_1_ID)
            .name(PRODUCT_1_NAME)
            .price(PRODUCT_1_PRICE)
            .description(PRODUCT_1_DESCRIPTION)
            .quantity(ITEM_1_QUANTITY)
            .build();
    }

    public static CartItemDto cartItemDto2() {
        return CartItemDto.builder()
            .id(CART_ITEM_2_ID)
            .productId(PRODUCT_2_ID)
            .name(PRODUCT_2_NAME)
            .price(PRODUCT_2_PRICE)
            .description(PRODUCT_2_DESCRIPTION)
            .quantity(ITEM_2_QUANTITY)
            .build();
    }

    public static AddCartItemRequest addCartItemRequest() {
        return AddCartItemRequest.builder()
            .productId(PRODUCT_1_ID)
            .quantity(ITEM_1_QUANTITY)
            .build();
    }

    public static CartItem cartItem1() {
        return CartItem.builder()
            .id(CART_ITEM_1_ID)
            .userId(USER_ID)
            .productId(PRODUCT_1_ID)
            .quantity(ITEM_1_QUANTITY)
            .build();
    }

    public static CartItem cartItem2() {
        return CartItem.builder()
            .id(CART_ITEM_2_ID)
            .userId(USER_ID)
            .productId(PRODUCT_2_ID)
            .quantity(ITEM_2_QUANTITY)
            .build();
    }

    public static OrdersResponse ordersResponse() {
        return OrdersResponse.builder()
            .orders(List.of(orderDto()))
            .build();
    }

    public static OrderDto orderDto() {
        return OrderDto.builder()
            .id(ORDER_ID)
            .userId(USER_ID)
            .status(OrderStatus.NEW)
            .totalPrice(TOTAL_PRICE)
            .dateTime(LocalDateTime.now(CLOCK))
            .orderItems(List.of(orderItemDto1(), orderItemDto2()))
            .history(new ArrayList<>(List.of(orderHistoryRecordDto())))
            .build();
    }

    public static OrderItemDto orderItemDto1() {
        return OrderItemDto.builder()
            .productId(PRODUCT_1_ID)
            .name(PRODUCT_1_NAME)
            .quantity(ITEM_1_QUANTITY)
            .price(PRODUCT_1_PRICE)
            .build();
    }

    public static OrderItemDto orderItemDto2() {
        return OrderItemDto.builder()
            .productId(PRODUCT_2_ID)
            .name(PRODUCT_2_NAME)
            .quantity(ITEM_2_QUANTITY)
            .price(PRODUCT_2_PRICE)
            .build();
    }

    public static OrderHistoryRecordDto orderHistoryRecordDto() {
        return OrderHistoryRecordDto.builder()
            .status(OrderStatus.NEW)
            .dateTime(LocalDateTime.now(CLOCK))
            .build();
    }

    public static Order order() {
        return Order.builder()
            .id(ORDER_ID)
            .userId(USER_ID)
            .status(OrderStatus.NEW)
            .dateTime(LocalDateTime.now(CLOCK))
            .orderItems(List.of(orderItem1(), orderItem2()))
            .history(new ArrayList<>(List.of(orderHistoryRecord())))
            .build();
    }

    public static OrderItem orderItem1() {
        return OrderItem.builder()
            .productId(PRODUCT_1_ID)
            .name(PRODUCT_1_NAME)
            .quantity(ITEM_1_QUANTITY)
            .price(PRODUCT_1_PRICE)
            .build();
    }

    public static OrderItem orderItem2() {
        return OrderItem.builder()
            .productId(PRODUCT_2_ID)
            .name(PRODUCT_2_NAME)
            .quantity(ITEM_2_QUANTITY)
            .price(PRODUCT_2_PRICE)
            .build();
    }

    public static OrderHistoryRecord orderHistoryRecord() {
        return OrderHistoryRecord.builder()
            .status(OrderStatus.NEW)
            .dateTime(LocalDateTime.now(CLOCK))
            .build();
    }

    public static PaymentUrlResponse paymentUrlResponse() {
        return PaymentUrlResponse.builder()
            .paymentUrl(PAYMENT_LINK)
            .build();
    }

    public static PaymentRequest paymentRequest() {
        return PaymentRequest.builder()
            .payerId(PAYER_ID)
            .paymentId(PAYMENT_ID)
            .build();
    }

    public static ProductsResponse productsResponse() {
        return ProductsResponse.builder()
            .products(List.of(productDto1(), productDto2()))
            .build();
    }

    public static ProductDto productDto1() {
        return ProductDto.builder()
            .id(PRODUCT_1_ID)
            .name(PRODUCT_1_NAME)
            .description(PRODUCT_1_DESCRIPTION)
            .price(PRODUCT_1_PRICE)
            .stockQuantity(PRODUCT_1_STOCK_QUANTITY)
            .build();
    }

    public static ProductDto productDto2() {
        return ProductDto.builder()
            .id(PRODUCT_2_ID)
            .name(PRODUCT_2_NAME)
            .description(PRODUCT_2_DESCRIPTION)
            .price(PRODUCT_2_PRICE)
            .stockQuantity(PRODUCT_2_STOCK_QUANTITY)
            .build();
    }

    public static Product product1() {
        return Product.builder()
            .id(PRODUCT_1_ID)
            .name(PRODUCT_1_NAME)
            .description(PRODUCT_1_DESCRIPTION)
            .price(PRODUCT_1_PRICE)
            .stockQuantity(PRODUCT_1_STOCK_QUANTITY)
            .build();
    }

    public static Product product2() {
        return Product.builder()
            .id(PRODUCT_2_ID)
            .name(PRODUCT_2_NAME)
            .description(PRODUCT_2_DESCRIPTION)
            .price(PRODUCT_2_PRICE)
            .stockQuantity(PRODUCT_2_STOCK_QUANTITY)
            .build();
    }

    public static List<Pair<CartItem, Product>> cartItemsWithProducts() {
        return List.of(
            Pair.of(cartItem1(), product1()),
            Pair.of(cartItem2(), product2())
        );
    }

    public static ValidationException validationException() {
        return new ValidationException(VALIDATION_MESSAGE);
    }

    public static String validationResponse() {
        return toJson(new ErrorResponse(BAD_REQUEST, VALIDATION_MESSAGE));
    }

    public static PayPalException payPalException() {
        PayPalRESTException cause = new PayPalRESTException(PAY_PAL_ERROR_MESSAGE);
        cause.setResponsecode(400);
        return new PayPalException(cause);
    }

    public static String payPalExceptionResponse() {
        return toJson(new ErrorResponse(BAD_REQUEST, PAY_PAL_ERROR_MESSAGE));
    }

    public static String unauthorizedResponse() {
        return toJson(new ErrorResponse(UNAUTHORIZED, UNAUTHORIZED_ERROR));
    }

    public static String forbiddenResponse() {
        return toJson(new ErrorResponse(FORBIDDEN, FORBIDDEN_ERROR));
    }

    public static OrderNotFoundException orderNotFoundException() {
        return new OrderNotFoundException(ORDER_ID);
    }

    public static String orderNotFoundResponse() {
        return toJson(new ErrorResponse(NOT_FOUND, ORDER_NOT_FOUND_MESSAGE));
    }

    public static ProductNotFoundException productNotFoundException() {
        return new ProductNotFoundException(PRODUCT_1_ID);
    }

    public static String productNotFoundResponse() {
        return toJson(new ErrorResponse(NOT_FOUND, PRODUCT_NOT_FOUND_MESSAGE));
    }

    public static String unexpectedErrorResponse() {
        return toJson(new ErrorResponse(INTERNAL_SERVER_ERROR, UNEXPECTED_ERROR));
    }

}
