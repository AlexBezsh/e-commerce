package com.alexbezsh.ecommerce.controller.api;

import com.alexbezsh.ecommerce.model.api.dto.OrderDto;
import com.alexbezsh.ecommerce.model.api.request.PaymentRequest;
import com.alexbezsh.ecommerce.model.api.response.OrdersResponse;
import com.alexbezsh.ecommerce.model.api.response.PaymentUrlResponse;
import com.alexbezsh.ecommerce.swagger.Default400And404And500Responses;
import com.alexbezsh.ecommerce.swagger.Default400And500Responses;
import com.alexbezsh.ecommerce.swagger.Default401And403Responses;
import com.alexbezsh.ecommerce.swagger.Default500Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import static com.alexbezsh.ecommerce.utils.SecurityUtils.USER_ROLE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "Orders")
@Secured(USER_ROLE)
@RequestMapping("/api/v1/orders")
@CrossOrigin({"${cors.allowed-origins}"})
public interface OrderApi {

    @Default500Response
    @Default401And403Responses
    @Operation(summary = "Get All Orders")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    OrdersResponse getAllOrders();

    @Default401And403Responses
    @Default400And500Responses
    @Operation(summary = "Create New Order")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(produces = APPLICATION_JSON_VALUE)
    OrderDto createOrder();

    @Default401And403Responses
    @Default400And404And500Responses
    @Operation(summary = "Cancel Order")
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(value = "/{id}/cancel", produces = APPLICATION_JSON_VALUE)
    OrderDto cancelOrder(@PathVariable("id") String id);

    @Default401And403Responses
    @Default400And404And500Responses
    @Operation(summary = "Initialize Payment")
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/{id}/payment", produces = APPLICATION_JSON_VALUE)
    PaymentUrlResponse initPayment(@PathVariable("id") String id);

    @Default401And403Responses
    @Default400And404And500Responses
    @Operation(summary = "Pay")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{id}/pay", consumes = APPLICATION_JSON_VALUE)
    void pay(@PathVariable("id") String id, @RequestBody @Valid PaymentRequest request);

}
