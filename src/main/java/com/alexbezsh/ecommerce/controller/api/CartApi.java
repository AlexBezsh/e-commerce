package com.alexbezsh.ecommerce.controller.api;

import com.alexbezsh.ecommerce.model.api.dto.CartDto;
import com.alexbezsh.ecommerce.model.api.dto.CartItemDto;
import com.alexbezsh.ecommerce.model.api.request.AddCartItemRequest;
import com.alexbezsh.ecommerce.swagger.Default400And404And500Responses;
import com.alexbezsh.ecommerce.swagger.Default401And403Responses;
import com.alexbezsh.ecommerce.swagger.Default500Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import static com.alexbezsh.ecommerce.utils.SecurityUtils.USER_ROLE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "Cart")
@Secured(USER_ROLE)
@RequestMapping("/api/v1/cart")
@CrossOrigin({"${cors.allowed-origins}"})
public interface CartApi {

    @Default500Response
    @Default401And403Responses
    @Operation(summary = "Get Cart")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    CartDto getUserCart();

    @Default401And403Responses
    @Default400And404And500Responses
    @Operation(summary = "Add Item to Cart")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    CartItemDto addCartItem(@RequestBody @Valid AddCartItemRequest request);

    @Default500Response
    @Default401And403Responses
    @Operation(summary = "Delete Cart Item")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    void deleteCartItem(@PathVariable("id") String id);

}
