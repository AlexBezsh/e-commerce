package com.alexbezsh.ecommerce.mapper;

import com.alexbezsh.ecommerce.model.api.dto.ProductDto;
import com.alexbezsh.ecommerce.model.db.products.Product;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    List<ProductDto> toDtos(List<Product> entities);

}
