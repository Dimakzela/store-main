package com.example.store.service;

import com.example.store.dto.CreateProductDTO;
import com.example.store.dto.ProductDTO;
import com.example.store.entity.Product;
import com.example.store.exception.NotFoundException;
import com.example.store.mapper.ProductMapper;
import com.example.store.repository.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, productMapper);
    }

    @Test
    void createProduct_WithValidInput_ReturnsCreatedOrderDTO() {
        CreateProductDTO inputDto = new CreateProductDTO();
        inputDto.setDescription("create product");

        Product unmappedProduct = new Product();
        unmappedProduct.setDescription("create product");

        Product savedProduct = new Product();
        savedProduct.setDescription("create product");
        savedProduct.setId(100L);

        ProductDTO expectedOutboundDto = new ProductDTO();
        expectedOutboundDto.setDescription("create product");
        expectedOutboundDto.setId(100L);

        when(productMapper.createDTOToProduct(inputDto)).thenReturn(unmappedProduct);
        when(productRepository.save(unmappedProduct)).thenReturn(savedProduct);
        when(productMapper.productToProductDTO(savedProduct)).thenReturn(expectedOutboundDto);

        ProductDTO result = productService.createProduct(inputDto);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("create product", result.getDescription());
        assertEquals(expectedOutboundDto, result);

        verify(productMapper, times(1)).createDTOToProduct(inputDto);
        verify(productRepository, times(1)).save(unmappedProduct);
        verify(productMapper, times(1)).productToProductDTO(savedProduct);
    }

    @Test
    void createProduct_WhenDatabaseThrowsException_PropagatesException() {
        CreateProductDTO inputDto = new CreateProductDTO();
        inputDto.setDescription("create product");

        Product unmappedProduct = new Product();
        unmappedProduct.setDescription("create product");

        when(productMapper.createDTOToProduct(inputDto)).thenReturn(unmappedProduct);

        when(productRepository.save(unmappedProduct)).thenThrow(new RuntimeException("Database connection timeout"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> productService.createProduct(inputDto));

        assertEquals("Database connection timeout", exception.getMessage());

        verify(productMapper, never()).productToProductDTO(any());
    }

    @Test
    void getAllProducts_WhenProductsExist_ReturnsDtoList() {
        Product product1 = new Product();
        Product product2 = new Product();
        List<Product> mockProducts = List.of(product1, product2);

        ProductDTO dto1 = new ProductDTO();
        ProductDTO dto2 = new ProductDTO();
        List<ProductDTO> expectedDtos = List.of(dto1, dto2);

        when(productRepository.findAll()).thenReturn(mockProducts);
        when(productMapper.productsToProductDTOs(mockProducts)).thenReturn(expectedDtos);

        List<ProductDTO> result = productService.getAllProducts();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedDtos, result);

        verify(productRepository, times(1)).findAll();
        verify(productMapper, times(1)).productsToProductDTOs(mockProducts);
    }

    @Test
    void getProductById_WhenProductExists_ReturnsProductResponseDTO() {
        Long productId = 44L;
        Product mockProduct = new Product();
        ProductDTO expectedDto = new ProductDTO();

        when(productRepository.findWithOrdersById(productId)).thenReturn(Optional.of(mockProduct));
        when(productMapper.productToProductDTO(mockProduct)).thenReturn(expectedDto);

        ProductDTO result = productService.getProductById(productId);

        assertNotNull(result);
        assertEquals(expectedDto, result);

        verify(productRepository, times(1)).findWithOrdersById(productId);
        verify(productMapper, times(1)).productToProductDTO(mockProduct);
    }

    @Test
    void getProductById_WhenProductDoesNotExist_ThrowsNotFoundException() {
        Long productId = 99L;
        when(productRepository.findWithOrdersById(productId)).thenReturn(Optional.empty());

        NotFoundException exception =
                assertThrows(NotFoundException.class, () -> productService.getProductById(productId));

        assertTrue(exception.getMessage().contains("Product"));
        assertTrue(exception.getMessage().contains(productId.toString()));

        verifyNoInteractions(productMapper);
    }
}
