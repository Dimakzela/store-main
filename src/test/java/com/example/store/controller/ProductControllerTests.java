package com.example.store.controller;

import com.example.store.dto.CreateProductDTO;
import com.example.store.dto.OrderCustomerDTO;
import com.example.store.dto.ProductDTO;
import com.example.store.exception.GlobalExceptionHandler;
import com.example.store.exception.NotFoundException;
import com.example.store.mapper.ProductMapper;
import com.example.store.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import(GlobalExceptionHandler.class)
@ComponentScan(basePackageClasses = {ProductMapper.class})
class ProductControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    private ProductDTO productDTO;
    private CreateProductDTO createProductDTO;

    @BeforeEach
    void setUp() {
        OrderCustomerDTO customerDTO = new OrderCustomerDTO();
        customerDTO.setName("John Doe");
        customerDTO.setId(1L);

        productDTO = new ProductDTO();
        productDTO.setDescription("Test Product");
        productDTO.setId(1L);
        productDTO.setOrderIds(List.of(1001L, 1002L));

        createProductDTO = new CreateProductDTO();
        createProductDTO.setDescription("Test Product");
    }

    @Test
    void createProduct_WhenPayloadIsValid_Returns201AndOrderDTO() throws Exception {

        when(productService.createProduct(any(CreateProductDTO.class))).thenReturn(productDTO);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createProductDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Test Product"));
    }

    @Test
    void createProduct_WhenDescriptionMissing_Returns400AndValidationMessage() throws Exception {
        CreateProductDTO invalidInputDto = new CreateProductDTO();
        createProductDTO.setDescription(null);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidInputDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Input validation failed"))
                .andExpect(jsonPath("$.validationErrors.description").value("Product description is required"));
    }

    @Test
    void getAllProducts_Returns200AndList() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of(productDTO));

        mockMvc.perform(get("/products").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").value("Test Product"))
                .andExpect(jsonPath("$[0].orderIds[0]").value(1001));
    }

    @Test
    void getProductById_WhenExists_Returns200() throws Exception {
        when(productService.getProductById(1L)).thenReturn(productDTO);

        mockMvc.perform(get("/products/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderIds").isArray());
    }

    @Test
    void getProductById_WhenMissing_Returns404() throws Exception {
        when(productService.getProductById(99L)).thenThrow(new NotFoundException("Product", 99L));

        mockMvc.perform(get("/products/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Product with ID 99 not found"));
    }
}
