package com.alexbezsh.ecommerce.service;

import com.alexbezsh.ecommerce.exception.ValidationException;
import com.alexbezsh.ecommerce.mapper.CartMapper;
import com.alexbezsh.ecommerce.model.api.dto.CartDto;
import com.alexbezsh.ecommerce.model.api.dto.CartItemDto;
import com.alexbezsh.ecommerce.model.api.request.AddCartItemRequest;
import com.alexbezsh.ecommerce.model.db.cartitems.CartItem;
import com.alexbezsh.ecommerce.model.db.products.Product;
import com.alexbezsh.ecommerce.repository.CartItemRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import static com.alexbezsh.ecommerce.model.Calculable.getTotal;
import static com.alexbezsh.ecommerce.utils.SecurityUtils.getUserId;
import static java.util.Collections.emptyList;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartMapper cartMapper;
    private final ProductService productService;
    private final CartItemRepository cartItemRepository;

    public CartDto getCart() {
        List<CartItemDto> dtos = getCartItems(false).stream()
            .map(pair -> cartMapper.toDto(pair.getFirst(), pair.getSecond()))
            .toList();
        return CartDto.builder()
            .cartItems(dtos)
            .totalPrice(getTotal(dtos))
            .build();
    }

    public CartItemDto add(AddCartItemRequest request) {
        String userId = getUserId();
        Product product = getValidProduct(userId, request);
        CartItem entity = cartMapper.toEntity(request);
        entity.setUserId(userId);
        return cartMapper.toDto(cartItemRepository.save(entity), product);
    }

    public List<Pair<CartItem, Product>> getCartItems(boolean removeFromStock) {
        List<CartItem> items = cartItemRepository.findByUserId(getUserId());
        if (items.isEmpty()) {
            return emptyList();
        }
        return productService.getAll(items, removeFromStock);
    }

    public void delete(String id) {
        cartItemRepository.deleteById(id);
    }

    public void deleteAll() {
        cartItemRepository.deleteByUserId(getUserId());
    }

    private Product getValidProduct(String userId, AddCartItemRequest request) {
        String productId = request.getProductId();
        Product product = productService.getById(productId);

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new ValidationException("Not enough items in stock");
        }

        if (cartItemRepository.findByUserIdAndProductId(userId, productId).isPresent()) {
            throw new ValidationException("This item is already present in the cart");
        }

        return product;
    }

}
