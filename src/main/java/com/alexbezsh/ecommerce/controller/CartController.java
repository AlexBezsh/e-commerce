package com.alexbezsh.ecommerce.controller;

import com.alexbezsh.ecommerce.controller.api.CartApi;
import com.alexbezsh.ecommerce.model.api.dto.CartDto;
import com.alexbezsh.ecommerce.model.api.dto.CartItemDto;
import com.alexbezsh.ecommerce.model.api.request.AddCartItemRequest;
import com.alexbezsh.ecommerce.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CartController implements CartApi {

    private final CartService cartService;

    @Override
    public CartDto getUserCart() {
        return cartService.getCart();
    }

    @Override
    public CartItemDto addCartItem(AddCartItemRequest request) {
        return cartService.add(request);
    }

    @Override
    public void deleteCartItem(String id) {
        cartService.delete(id);
    }

}
