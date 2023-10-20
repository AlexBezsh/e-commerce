package com.alexbezsh.ecommerce.controller.api;

import com.alexbezsh.ecommerce.model.api.response.ProductsResponse;
import com.alexbezsh.ecommerce.swagger.Default500Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "Products")
@RequestMapping("/api/v1/products")
@CrossOrigin({"${cors.allowed-origins}"})
public interface ProductApi {

    @Default500Response
    @Operation(summary = "Get All Products")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    ProductsResponse getAllProducts();

}
