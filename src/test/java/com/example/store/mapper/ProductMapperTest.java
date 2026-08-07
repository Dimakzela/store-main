package com.example.store.mapper;

import com.example.store.dto.CreateProductDTO;
import com.example.store.dto.ProductDTO;
import com.example.store.entity.Order;
import com.example.store.entity.Product;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductMapperTest {

    private final ProductMapper productMapper = new ProductMapperImpl();

    @Test
    void productToProductDTO_WhenProductHasOrder_MapsSuccessfully() {
        Order mockOrder = new Order();
        mockOrder.setId(500L);

        Product product = new Product();
        product.setId(10L);
        product.setDescription("Gaming Mouse");
        product.setOrders(List.of(mockOrder));

        ProductDTO result = productMapper.productToProductDTO(product);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Gaming Mouse", result.getDescription());
        assertNotNull(result.getOrderIds());
        assertEquals(1, result.getOrderIds().size());
        assertEquals(500L, result.getOrderIds().get(0));
    }

    @Test
    void productToProductDTO_WhenProductHasNoOrder_ReturnsEmptyOrderIdsList() {
        Product product = new Product();
        product.setId(20L);
        product.setDescription("Unordered Keyboard");
        product.setOrders(null);

        ProductDTO result = productMapper.productToProductDTO(product);

        assertNotNull(result);
        assertEquals(20L, result.getId());
        assertNotNull(result.getOrderIds());
        assertTrue(result.getOrderIds().isEmpty());
    }

    @Test
    void productToProductDTO_WhenProductIsNull_ReturnsNull() {
        ProductDTO result = productMapper.productToProductDTO(null);

        assertNull(result);
    }

    @Test
    void createDTOToProduct_MapsCleanly() {
        CreateProductDTO dto = new CreateProductDTO();
        dto.setDescription("New Monitor");

        Product result = productMapper.createDTOToProduct(dto);

        assertNotNull(result);
        assertNull(result.getId());
        assertEquals("New Monitor", result.getDescription());
    }

    @Test
    void productsToProductDTOs_MapsCollections() {
        Product p1 = new Product();
        p1.setId(1L);
        Product p2 = new Product();
        p2.setId(2L);
        List<Product> products = List.of(p1, p2);

        List<ProductDTO> result = productMapper.productsToProductDTOs(products);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
    }
}
