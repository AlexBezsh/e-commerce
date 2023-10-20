package com.alexbezsh.ecommerce.mapper;

import com.alexbezsh.ecommerce.model.api.dto.ProductDto;
import com.alexbezsh.ecommerce.model.db.products.Product;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import static com.alexbezsh.ecommerce.TestUtils.product1;
import static com.alexbezsh.ecommerce.TestUtils.product2;
import static com.alexbezsh.ecommerce.TestUtils.productDto1;
import static com.alexbezsh.ecommerce.TestUtils.productDto2;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProductMapperTest {

    private final ProductMapper testedInstance = Mappers.getMapper(ProductMapper.class);

    @Test
    void toDtos() {
        List<Product> entities = List.of(product1(), product2());
        List<ProductDto> expected = List.of(productDto1(), productDto2());

        List<ProductDto> actual = testedInstance.toDtos(entities);

        assertEquals(expected, actual);
    }

    @Test
    void toDtosShouldReturnNull() {
        assertNull(testedInstance.toDtos(null));
    }

    @Test
    void toDtosShouldReturnEmptyList() {
        assertEquals(emptyList(), testedInstance.toDtos(emptyList()));
    }

}
