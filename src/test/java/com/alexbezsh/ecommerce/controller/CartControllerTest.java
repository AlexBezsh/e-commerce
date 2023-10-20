package com.alexbezsh.ecommerce.controller;

import com.alexbezsh.ecommerce.model.api.dto.CartDto;
import com.alexbezsh.ecommerce.model.api.dto.CartItemDto;
import com.alexbezsh.ecommerce.model.api.request.AddCartItemRequest;
import com.alexbezsh.ecommerce.model.api.response.ErrorResponse;
import org.junit.jupiter.api.Test;
import static com.alexbezsh.ecommerce.TestRole.TEST;
import static com.alexbezsh.ecommerce.TestRole.USER;
import static com.alexbezsh.ecommerce.TestUtils.CART_ITEM_1_ID;
import static com.alexbezsh.ecommerce.TestUtils.addCartItemRequest;
import static com.alexbezsh.ecommerce.TestUtils.cartDto;
import static com.alexbezsh.ecommerce.TestUtils.cartItemDto1;
import static com.alexbezsh.ecommerce.TestUtils.forbiddenResponse;
import static com.alexbezsh.ecommerce.TestUtils.productNotFoundException;
import static com.alexbezsh.ecommerce.TestUtils.productNotFoundResponse;
import static com.alexbezsh.ecommerce.TestUtils.toJson;
import static com.alexbezsh.ecommerce.TestUtils.unauthorizedResponse;
import static com.alexbezsh.ecommerce.TestUtils.unexpectedErrorResponse;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CartControllerTest extends AbstractControllerTest {

    private static final String BASE_URL = "/api/v1/cart";
    private static final String CART_ITEM_URL = BASE_URL + "/" + CART_ITEM_1_ID;

    @Test
    void getUserCart() throws Exception {
        CartDto expected = cartDto();

        doReturn(expected).when(cartService).getCart();

        mockMvc.perform(get(BASE_URL)
                .with(jwt().authorities(USER)))
            .andExpect(status().isOk())
            .andExpect(content().json(toJson(expected)));
    }

    @Test
    void getUserCartShouldReturn401() throws Exception {
        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isUnauthorized())
            .andExpect(content().json(unauthorizedResponse()));
    }

    @Test
    void getUserCartShouldReturn403() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .with(jwt().authorities(TEST)))
            .andExpect(status().isForbidden())
            .andExpect(content().json(forbiddenResponse()));
    }

    @Test
    void getUserCartShouldReturn500() throws Exception {
        doThrow(new RuntimeException()).when(cartService).getCart();

        mockMvc.perform(get(BASE_URL)
                .with(jwt().authorities(USER)))
            .andExpect(status().isInternalServerError())
            .andExpect(content().json(unexpectedErrorResponse()));
    }

    @Test
    void addCartItem() throws Exception {
        AddCartItemRequest request = addCartItemRequest();
        CartItemDto expected = cartItemDto1();

        doReturn(expected).when(cartService).add(request);

        mockMvc.perform(post(BASE_URL)
                .content(toJson(request))
                .contentType(APPLICATION_JSON)
                .with(jwt().authorities(USER)))
            .andExpect(status().isCreated())
            .andExpect(content().json(toJson(expected)));
    }

    @Test
    void addCartItemShouldReturn400() throws Exception {
        AddCartItemRequest request = addCartItemRequest();
        request.setProductId(null);
        ErrorResponse expected = new ErrorResponse(BAD_REQUEST, "productId: must not be blank");

        mockMvc.perform(post(BASE_URL)
                .content(toJson(request))
                .contentType(APPLICATION_JSON)
                .with(jwt().authorities(USER)))
            .andExpect(status().isBadRequest())
            .andExpect(content().json(toJson(expected)));
    }

    @Test
    void addCartItemShouldReturn401() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .content(toJson(addCartItemRequest()))
                .contentType(APPLICATION_JSON)
                .with(csrf()))
            .andExpect(status().isUnauthorized())
            .andExpect(content().json(unauthorizedResponse()));
    }

    @Test
    void addCartItemShouldReturn403() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .content(toJson(addCartItemRequest()))
                .contentType(APPLICATION_JSON)
                .with(jwt().authorities(TEST)))
            .andExpect(status().isForbidden())
            .andExpect(content().json(forbiddenResponse()));
    }

    @Test
    void
    addCartItemShouldReturn404() throws Exception {
        AddCartItemRequest request = addCartItemRequest();

        doThrow(productNotFoundException()).when(cartService).add(request);

        mockMvc.perform(post(BASE_URL)
                .content(toJson(request))
                .contentType(APPLICATION_JSON)
                .with(jwt().authorities(USER)))
            .andExpect(status().isNotFound())
            .andExpect(content().json(productNotFoundResponse()));
    }

    @Test
    void addCartItemShouldReturn500() throws Exception {
        AddCartItemRequest request = addCartItemRequest();

        doThrow(RuntimeException.class).when(cartService).add(request);

        mockMvc.perform(post(BASE_URL)
                .content(toJson(request))
                .contentType(APPLICATION_JSON)
                .with(jwt().authorities(USER)))
            .andExpect(status().isInternalServerError())
            .andExpect(content().json(unexpectedErrorResponse()));
    }

    @Test
    void deleteCartItem() throws Exception {
        mockMvc.perform(delete(CART_ITEM_URL)
                .with(jwt().authorities(USER)))
            .andExpect(status().isNoContent());

        verify(cartService).delete(CART_ITEM_1_ID);
    }

    @Test
    void deleteCartItemShouldReturn401() throws Exception {
        mockMvc.perform(delete(CART_ITEM_URL).with(csrf()))
            .andExpect(status().isUnauthorized())
            .andExpect(content().json(unauthorizedResponse()));
    }

    @Test
    void deleteCartItemShouldReturn403() throws Exception {
        mockMvc.perform(delete(CART_ITEM_URL)
                .with(jwt().authorities(TEST)))
            .andExpect(status().isForbidden())
            .andExpect(content().json(forbiddenResponse()));
    }

    @Test
    void deleteCartItemShouldReturn500() throws Exception {
        doThrow(RuntimeException.class).when(cartService).delete(CART_ITEM_1_ID);

        mockMvc.perform(delete(CART_ITEM_URL)
                .with(jwt().authorities(USER)))
            .andExpect(status().isInternalServerError())
            .andExpect(content().json(unexpectedErrorResponse()));
    }

}
