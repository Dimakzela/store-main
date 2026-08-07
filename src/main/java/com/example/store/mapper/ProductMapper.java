package com.example.store.mapper;

import com.example.store.dto.CreateProductDTO;
import com.example.store.dto.ProductDTO;
import com.example.store.entity.Order;
import com.example.store.entity.Product;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(source = "orders", target = "orderIds")
    ProductDTO productToProductDTO(Product product);

    Product createDTOToProduct(CreateProductDTO dto);

    List<ProductDTO> productsToProductDTOs(List<Product> products);

    default List<Long> mapOrdersToIds(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return Collections.emptyList();
        }
        return orders.stream().map(Order::getId).collect(Collectors.toList());
    }
}
