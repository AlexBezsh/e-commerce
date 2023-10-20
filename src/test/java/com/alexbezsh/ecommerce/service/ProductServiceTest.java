package com.alexbezsh.ecommerce.service;

import com.alexbezsh.ecommerce.exception.ValidationException;
import com.alexbezsh.ecommerce.exception.notfound.ProductNotFoundException;
import com.alexbezsh.ecommerce.mapper.ProductMapper;
import com.alexbezsh.ecommerce.model.api.response.ProductsResponse;
import com.alexbezsh.ecommerce.model.db.cartitems.CartItem;
import com.alexbezsh.ecommerce.model.db.orders.OrderItem;
import com.alexbezsh.ecommerce.model.db.products.Product;
import com.alexbezsh.ecommerce.repository.ProductRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;
import static com.alexbezsh.ecommerce.TestUtils.ITEM_1_QUANTITY;
import static com.alexbezsh.ecommerce.TestUtils.ITEM_2_QUANTITY;
import static com.alexbezsh.ecommerce.TestUtils.PRODUCT_1_ID;
import static com.alexbezsh.ecommerce.TestUtils.PRODUCT_1_NAME;
import static com.alexbezsh.ecommerce.TestUtils.PRODUCT_1_STOCK_QUANTITY;
import static com.alexbezsh.ecommerce.TestUtils.PRODUCT_2_ID;
import static com.alexbezsh.ecommerce.TestUtils.PRODUCT_NOT_FOUND_MESSAGE;
import static com.alexbezsh.ecommerce.TestUtils.cartItem1;
import static com.alexbezsh.ecommerce.TestUtils.cartItem2;
import static com.alexbezsh.ecommerce.TestUtils.cartItemsWithProducts;
import static com.alexbezsh.ecommerce.TestUtils.orderItem1;
import static com.alexbezsh.ecommerce.TestUtils.orderItem2;
import static com.alexbezsh.ecommerce.TestUtils.product1;
import static com.alexbezsh.ecommerce.TestUtils.product2;
import static com.alexbezsh.ecommerce.TestUtils.productsResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    private ProductService testedInstance;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductRepository productRepository;

    @Test
    void getAll() {
        List<Product> entities = List.of(product1(), product2());
        ProductsResponse expected = productsResponse();

        doReturn(entities).when(productRepository).findAll();
        doReturn(expected.getProducts()).when(productMapper).toDtos(entities);

        ProductsResponse actual = testedInstance.getAll();

        assertEquals(expected, actual);
    }

    @Test
    void getAllForCartItemsWithoutRemoval() {
        List<CartItem> cartItems = List.of(cartItem1(), cartItem2());
        List<Product> products = List.of(product1(), product2());
        List<Pair<CartItem, Product>> expected = cartItemsWithProducts();

        doReturn(products).when(productRepository).findAllById(List.of(PRODUCT_1_ID, PRODUCT_2_ID));

        List<Pair<CartItem, Product>> actual = testedInstance.getAll(cartItems, false);

        assertEquals(expected, actual);

        verify(productRepository, never()).saveAll(any());
    }

    @Test
    void getAllForCartItemsWithRemoval() {
        List<CartItem> cartItems = List.of(cartItem1(), cartItem2());
        List<Product> products = List.of(product1(), product2());
        List<Pair<CartItem, Product>> expected = cartItemsWithProducts();
        Product expectedProduct1 = expected.get(0).getSecond();
        expectedProduct1.setStockQuantity(expectedProduct1.getStockQuantity() - ITEM_1_QUANTITY);
        Product expectedProduct2 = expected.get(1).getSecond();
        expectedProduct2.setStockQuantity(expectedProduct2.getStockQuantity() - ITEM_2_QUANTITY);
        List<Product> expectedProducts = List.of(expectedProduct1, expectedProduct2);

        doReturn(products).when(productRepository).findAllById(List.of(PRODUCT_1_ID, PRODUCT_2_ID));

        List<Pair<CartItem, Product>> actual = testedInstance.getAll(cartItems, true);

        assertEquals(expected, actual);

        verify(productRepository).saveAll(expectedProducts);
    }

    @Test
    void getAllForCartItemsShouldThrowNotFoundException() {
        List<CartItem> cartItems = List.of(cartItem1(), cartItem2());
        List<Product> products = List.of(product2());

        doReturn(products).when(productRepository).findAllById(List.of(PRODUCT_1_ID, PRODUCT_2_ID));

        ProductNotFoundException e = assertThrows(ProductNotFoundException.class,
            () -> testedInstance.getAll(cartItems, false));

        assertEquals(PRODUCT_NOT_FOUND_MESSAGE, e.getMessage());

        verify(productRepository, never()).saveAll(any());
    }

    @Test
    void getAllForCartItemsShouldThrowValidationException() {
        CartItem cartItem = cartItem1();
        cartItem.setQuantity(PRODUCT_1_STOCK_QUANTITY + 1);
        List<CartItem> cartItems = List.of(cartItem);
        List<Product> products = List.of(product1());
        String expected = String.format("Not enough items in stock. Product ID: %s. "
            + "Product name: %s", PRODUCT_1_ID, PRODUCT_1_NAME);

        doReturn(products).when(productRepository).findAllById(List.of(PRODUCT_1_ID));

        ValidationException e = assertThrows(ValidationException.class,
            () -> testedInstance.getAll(cartItems, true));

        assertEquals(expected, e.getMessage());

        verify(productRepository, never()).saveAll(any());
    }

    @Test
    void getById() {
        Product expected = product1();

        doReturn(Optional.of(expected)).when(productRepository).findById(PRODUCT_1_ID);

        Product actual = testedInstance.getById(PRODUCT_1_ID);

        assertEquals(expected, actual);
    }

    @Test
    void getByIdShouldThrowNotFoundException() {
        doReturn(Optional.empty()).when(productRepository).findById(PRODUCT_1_ID);

        assertThrows(ProductNotFoundException.class, () -> testedInstance.getById(PRODUCT_1_ID));
    }

    @Test
    void addOrderedItemsToStock() {
        List<OrderItem> orderItems = List.of(orderItem1(), orderItem2());
        List<Product> products = List.of(product1(), product2());
        Product updatedProduct1 = product1();
        updatedProduct1.setStockQuantity(updatedProduct1.getStockQuantity() + ITEM_1_QUANTITY);
        Product updatedProduct2 = product2();
        updatedProduct2.setStockQuantity(updatedProduct2.getStockQuantity() + ITEM_2_QUANTITY);
        List<Product> expected = List.of(updatedProduct1, updatedProduct2);

        doReturn(products).when(productRepository).findAllById(List.of(PRODUCT_1_ID, PRODUCT_2_ID));

        testedInstance.addOrderedItemsToStock(orderItems);

        verify(productRepository).saveAll(expected);
    }

    @Test
    void addOrderedItemsToStockShouldThrowNotFoundException() {
        List<OrderItem> orderItems = List.of(orderItem1(), orderItem2());
        List<Product> products = List.of(product2());

        doReturn(products).when(productRepository).findAllById(List.of(PRODUCT_1_ID, PRODUCT_2_ID));

        ProductNotFoundException e = assertThrows(ProductNotFoundException.class,
            () -> testedInstance.addOrderedItemsToStock(orderItems));

        assertEquals(PRODUCT_NOT_FOUND_MESSAGE, e.getMessage());

        verify(productRepository, never()).saveAll(any());
    }

}
