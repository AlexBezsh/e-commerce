package com.alexbezsh.ecommerce.service;

import com.alexbezsh.ecommerce.exception.ValidationException;
import com.alexbezsh.ecommerce.mapper.CartMapper;
import com.alexbezsh.ecommerce.model.api.dto.CartDto;
import com.alexbezsh.ecommerce.model.api.dto.CartItemDto;
import com.alexbezsh.ecommerce.model.api.request.AddCartItemRequest;
import com.alexbezsh.ecommerce.model.db.cartitems.CartItem;
import com.alexbezsh.ecommerce.model.db.products.Product;
import com.alexbezsh.ecommerce.repository.CartItemRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;
import org.springframework.security.core.context.SecurityContextHolder;
import static com.alexbezsh.ecommerce.TestUtils.CART_ITEM_1_ID;
import static com.alexbezsh.ecommerce.TestUtils.PRODUCT_1_ID;
import static com.alexbezsh.ecommerce.TestUtils.USER_ID;
import static com.alexbezsh.ecommerce.TestUtils.addCartItemRequest;
import static com.alexbezsh.ecommerce.TestUtils.cartDto;
import static com.alexbezsh.ecommerce.TestUtils.cartItem1;
import static com.alexbezsh.ecommerce.TestUtils.cartItem2;
import static com.alexbezsh.ecommerce.TestUtils.cartItemDto1;
import static com.alexbezsh.ecommerce.TestUtils.cartItemDto2;
import static com.alexbezsh.ecommerce.TestUtils.cartItemsWithProducts;
import static com.alexbezsh.ecommerce.TestUtils.product1;
import static com.alexbezsh.ecommerce.TestUtils.product2;
import static com.alexbezsh.ecommerce.utils.SecurityUtilsTest.mockUser;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @InjectMocks
    private CartService testedInstance;

    @Mock
    private CartMapper cartMapper;

    @Mock
    private ProductService productService;

    @Mock
    private CartItemRepository cartItemRepository;

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCart() {
        List<CartItem> cartItems = List.of(cartItem1(), cartItem2());
        CartDto expected = cartDto();

        mockUser();
        doReturn(cartItems).when(cartItemRepository).findByUserId(USER_ID);
        doReturn(cartItemsWithProducts()).when(productService).getAll(cartItems, false);
        doReturn(cartItemDto1()).when(cartMapper).toDto(cartItem1(), product1());
        doReturn(cartItemDto2()).when(cartMapper).toDto(cartItem2(), product2());

        CartDto actual = testedInstance.getCart();

        assertEquals(expected, actual);
    }

    @Test
    void getEmptyCart() {
        List<CartItem> cartItems = emptyList();
        CartDto expected = CartDto.builder()
            .cartItems(emptyList())
            .totalPrice(BigDecimal.ZERO)
            .build();

        mockUser();
        doReturn(cartItems).when(cartItemRepository).findByUserId(USER_ID);

        CartDto actual = testedInstance.getCart();

        assertEquals(expected, actual);

        verify(productService, never()).getAll(any(), anyBoolean());
    }

    @Test
    void addCartItem() {
        AddCartItemRequest request = addCartItemRequest();
        Product product = product1();
        CartItem cartItem = cartItem1();
        cartItem.setUserId(null);
        CartItem cartItemWithUser = cartItem1();
        CartItemDto expected = cartItemDto1();

        mockUser();
        doReturn(product).when(productService).getById(PRODUCT_1_ID);
        doReturn(Optional.empty()).when(cartItemRepository)
            .findByUserIdAndProductId(USER_ID, PRODUCT_1_ID);
        doReturn(cartItem).when(cartMapper).toEntity(request);
        doReturn(cartItemWithUser).when(cartItemRepository).save(cartItemWithUser);
        doReturn(expected).when(cartMapper).toDto(cartItemWithUser, product);

        CartItemDto actual = testedInstance.add(request);

        assertEquals(expected, actual);
    }

    @Test
    void addCartItemShouldThrowValidationExceptionIfNotEnoughItemsInStock() {
        AddCartItemRequest request = addCartItemRequest();
        Product product = product1();
        product.setStockQuantity(0);

        mockUser();
        doReturn(product).when(productService).getById(PRODUCT_1_ID);

        ValidationException exception = assertThrows(ValidationException.class,
            () -> testedInstance.add(request));

        assertEquals("Not enough items in stock", exception.getMessage());
    }

    @Test
    void addCartItemShouldThrowValidationExceptionIfItemAlreadyInCart() {
        AddCartItemRequest request = addCartItemRequest();
        Product product = product1();

        mockUser();
        doReturn(product).when(productService).getById(PRODUCT_1_ID);
        doReturn(Optional.of(cartItemDto1())).when(cartItemRepository)
            .findByUserIdAndProductId(USER_ID, PRODUCT_1_ID);

        ValidationException exception = assertThrows(ValidationException.class,
            () -> testedInstance.add(request));

        assertEquals("This item is already present in the cart", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getCartItems(boolean removeFromStock) {
        List<CartItem> cartItems = List.of(cartItem1(), cartItem2());
        List<Pair<CartItem, Product>> expected = cartItemsWithProducts();

        mockUser();
        doReturn(cartItems).when(cartItemRepository).findByUserId(USER_ID);
        doReturn(expected).when(productService).getAll(cartItems, removeFromStock);

        List<Pair<CartItem, Product>> actual = testedInstance.getCartItems(removeFromStock);

        assertEquals(expected, actual);
    }

    @Test
    void getCartItemsShouldReturnEmptyList() {
        mockUser();
        doReturn(emptyList()).when(cartItemRepository).findByUserId(USER_ID);

        List<Pair<CartItem, Product>> actual = testedInstance.getCartItems(false);

        assertEquals(emptyList(), actual);

        verify(productService, never()).getAll(any(), anyBoolean());
    }

    @Test
    void delete() {
        testedInstance.delete(CART_ITEM_1_ID);

        verify(cartItemRepository).deleteById(CART_ITEM_1_ID);
    }

    @Test
    void deleteAll() {
        mockUser();

        testedInstance.deleteAll();

        verify(cartItemRepository).deleteByUserId(USER_ID);
    }

}
