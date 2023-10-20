package com.alexbezsh.ecommerce.exception.notfound;

public class OrderNotFoundException extends NotFoundException {

    public OrderNotFoundException(String id) {
        super("Order " + id + " not found");
    }

}
