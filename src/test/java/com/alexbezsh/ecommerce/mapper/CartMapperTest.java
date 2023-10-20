package com.alexbezsh.ecommerce.mapper;

import com.alexbezsh.ecommerce.model.api.dto.CartItemDto;
import com.alexbezsh.ecommerce.model.api.request.AddCartItemRequest;
import com.alexbezsh.ecommerce.model.db.cartitems.CartItem;
import com.alexbezsh.ecommerce.model.db.products.Product;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import static com.alexbezsh.ecommerce.TestUtils.ITEM_1_QUANTITY;
import static com.alexbezsh.ecommerce.TestUtils.PRODUCT_1_ID;
import static com.alexbezsh.ecommerce.TestUtils.addCartItemRequest;
import static com.alexbezsh.ecommerce.TestUtils.cartItem1;
import static com.alexbezsh.ecommerce.TestUtils.cartItemDto1;
import static com.alexbezsh.ecommerce.TestUtils.product1;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CartMapperTest {

    private final CartMapper testedInstance = Mappers.getMapper(CartMapper.class);

    @Test
    void toDto() {
        Product product = product1();
        CartItem cartItem = cartItem1();
        CartItemDto expected = cartItemDto1();

        CartItemDto actual = testedInstance.toDto(cartItem, product);

        assertEquals(expected, actual);
    }

    @Test
    void toEntity() {
        AddCartItemRequest request = addCartItemRequest();
        CartItem expected = CartItem.builder()
            .productId(PRODUCT_1_ID)
            .quantity(ITEM_1_QUANTITY)
            .build();

        CartItem actual = testedInstance.toEntity(request);

        assertEquals(expected, actual);
    }

}
