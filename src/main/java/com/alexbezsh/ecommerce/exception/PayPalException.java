package com.alexbezsh.ecommerce.exception;

import com.paypal.base.rest.PayPalRESTException;
import lombok.Getter;

@Getter
public class PayPalException extends RuntimeException {

    private final PayPalRESTException cause;

    public PayPalException(PayPalRESTException cause) {
        this.cause = cause;
    }

}
