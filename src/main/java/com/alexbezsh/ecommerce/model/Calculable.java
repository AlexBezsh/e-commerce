package com.alexbezsh.ecommerce.model;

import java.math.BigDecimal;
import java.util.List;

public interface Calculable {

    int getQuantity();

    BigDecimal getPrice();

    default BigDecimal getSubtotal() {
        return getPrice().multiply(new BigDecimal(getQuantity()));
    }

    static BigDecimal getTotal(List<? extends Calculable> items) {
        return items.stream()
            .map(Calculable::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
