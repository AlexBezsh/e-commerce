package com.alexbezsh.ecommerce.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import static com.alexbezsh.ecommerce.TestUtils.PRODUCT_2_PRICE;
import static com.alexbezsh.ecommerce.TestUtils.TOTAL_PRICE;
import static com.alexbezsh.ecommerce.TestUtils.cartItemDto1;
import static com.alexbezsh.ecommerce.TestUtils.cartItemDto2;
import static com.alexbezsh.ecommerce.TestUtils.orderItem1;
import static com.alexbezsh.ecommerce.TestUtils.orderItem2;
import static com.alexbezsh.ecommerce.TestUtils.orderItemDto2;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculableTest {

    @ParameterizedTest
    @CsvSource({
        "1,2,2",
        "1,2.75,2.75",
        "10,1.1,11",
        "10,2,20",
        "2,4.75,9.5"})
    void getSubtotal(int quantity, BigDecimal price, BigDecimal expected) {
        TestClass test = new TestClass(quantity, price);

        assertEquals(0, expected.compareTo(test.getSubtotal()));
    }

    @ParameterizedTest
    @MethodSource("getTotalPriceArgs")
    void getTotal(List<? extends Calculable> items, BigDecimal expected) {
        assertEquals(0, expected.compareTo(Calculable.getTotal(items)));
    }

    private static Stream<Arguments> getTotalPriceArgs() {
        return Stream.of(
            Arguments.of(List.of(cartItemDto1(), cartItemDto2()), TOTAL_PRICE),
            Arguments.of(List.of(orderItem1(), orderItem2()), TOTAL_PRICE),
            Arguments.of(List.of(orderItemDto2()), PRODUCT_2_PRICE),
            Arguments.of(List.of(), BigDecimal.ZERO)
        );
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class TestClass implements Calculable {

        private int quantity;
        private BigDecimal price;

    }

}
