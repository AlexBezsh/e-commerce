package com.alexbezsh.ecommerce.controller;

import com.alexbezsh.ecommerce.model.api.response.ProductsResponse;
import org.junit.jupiter.api.Test;
import static com.alexbezsh.ecommerce.TestUtils.productsResponse;
import static com.alexbezsh.ecommerce.TestUtils.toJson;
import static com.alexbezsh.ecommerce.TestUtils.unexpectedErrorResponse;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductControllerTest extends AbstractControllerTest {

    private static final String BASE_URL = "/api/v1/products";

    @Test
    public void getAllOrders() throws Exception {
        ProductsResponse expected = productsResponse();

        doReturn(expected).when(productService).getAll();

        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isOk())
            .andExpect(content().json(toJson(expected)));
    }

    @Test
    public void getAllOrdersShouldReturn500() throws Exception {
        doThrow(RuntimeException.class).when(productService).getAll();

        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isInternalServerError())
            .andExpect(content().json(unexpectedErrorResponse()));
    }

}
