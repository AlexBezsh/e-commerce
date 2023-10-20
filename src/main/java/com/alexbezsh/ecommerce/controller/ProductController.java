package com.alexbezsh.ecommerce.controller;

import com.alexbezsh.ecommerce.controller.api.ProductApi;
import com.alexbezsh.ecommerce.model.api.response.ProductsResponse;
import com.alexbezsh.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProductController implements ProductApi {

    private final ProductService productService;

    @Override
    public ProductsResponse getAllProducts() {
        return productService.getAll();
    }

}
