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
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;
    private final ProductRepository productRepository;

    public ProductsResponse getAll() {
        return ProductsResponse.builder()
            .products(productMapper.toDtos(productRepository.findAll()))
            .build();
    }

    public List<Pair<CartItem, Product>> getAll(List<CartItem> items, boolean removeFromStock) {
        List<Product> products = productRepository.findAllById(items.stream()
            .map(CartItem::getProductId)
            .toList());
        Stream<Pair<CartItem, Product>> stream = items.stream()
            .map(item -> Pair.of(item, getProduct(item.getProductId(), products)));
        if (removeFromStock) {
            var result = stream.peek(this::removeFromStock).toList();
            productRepository.saveAll(products);
            return result;
        }
        return stream.toList();
    }

    public Product getById(String id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public void addOrderedItemsToStock(List<OrderItem> items) {
        List<Product> products = productRepository.findAllById(items.stream()
            .map(OrderItem::getProductId)
            .toList());
        items.forEach(item -> {
            Product product = getProduct(item.getProductId(), products);
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
        });
        productRepository.saveAll(products);
    }

    private Product getProduct(String productId, List<Product> products) {
        return products.stream()
            .filter(p -> p.getId().equals(productId))
            .findFirst()
            .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private void removeFromStock(Pair<CartItem, Product> pair) {
        CartItem item = pair.getFirst();
        Product product = pair.getSecond();
        if (product.getStockQuantity() < item.getQuantity()) {
            throw new ValidationException(String.format("Not enough items in stock. "
                + "Product ID: %s. Product name: %s", product.getId(), product.getName()));
        }
        product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
    }

}
