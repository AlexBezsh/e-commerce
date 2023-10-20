package com.alexbezsh.ecommerce.exception.notfound;

public class ProductNotFoundException extends NotFoundException {

    public ProductNotFoundException(String id) {
        super("Product " + id + " not found");
    }

}
