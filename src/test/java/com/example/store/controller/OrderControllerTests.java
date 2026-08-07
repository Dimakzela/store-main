package com.example.store.controller;

import com.example.store.dto.CreateOrderDTO;
import com.example.store.dto.OrderCustomerDTO;
import com.example.store.dto.OrderDTO;
import com.example.store.dto.OrderProductDTO;
import com.example.store.exception.GlobalExceptionHandler;
import com.example.store.exception.NotFoundException;
import com.example.store.mapper.OrderMapper;
import com.example.store.service.OrderService;
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

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@Import(GlobalExceptionHandler.class)
@ComponentScan(basePackageClasses = {OrderMapper.class})
class OrderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    private OrderDTO orderDTO;
    private CreateOrderDTO createOrderDTO;

    @BeforeEach
    void setUp() {
        OrderCustomerDTO customerDTO = new OrderCustomerDTO();
        customerDTO.setName("John Doe");
        customerDTO.setId(1L);

        OrderProductDTO orderProductDTO = new OrderProductDTO();
        orderProductDTO.setId(99L);
        orderProductDTO.setDescription("Logitech Mouse");

        orderDTO = new OrderDTO();
        orderDTO.setDescription("Test Order");
        orderDTO.setId(1L);
        orderDTO.setCustomer(customerDTO);
        orderDTO.setProducts(List.of(orderProductDTO));

        createOrderDTO = new CreateOrderDTO();
        createOrderDTO.setCustomerId(1L);
        createOrderDTO.setDescription("Test Order");
        createOrderDTO.setProductIds(List.of(99L));
    }

    @Test
    void getAllOrders_Returns200AndOrderList() throws Exception {
        when(orderService.getAllOrders()).thenReturn(Collections.singletonList(orderDTO));

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(orderDTO.getId()))
                .andExpect(jsonPath("$[0].description").value("Test Order"))
                .andExpect(jsonPath("$[0].customer.name").value("John Doe"));
    }

    @Test
    void getOrderById_WhenOrderExists_Returns200AndOrderDTO() throws Exception {
        Long targetId = 1L;
        when(orderService.getOrderById(targetId)).thenReturn(orderDTO);

        mockMvc.perform(get("/orders/{id}", targetId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(targetId))
                .andExpect(jsonPath("$.description").value("Test Order"))
                .andExpect(jsonPath("$.customer.name").value("John Doe"));
    }

    @Test
    void getOrder_WhenOrderDoesNotExist_Returns404FromGlobalHandler() throws Exception {
        Long targetId = -1L;
        when(orderService.getOrderById(targetId)).thenThrow(new NotFoundException("Order", targetId));

        mockMvc.perform(get("/orders/{id}", targetId).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Order with ID -1 not found"));
    }

    @Test
    void createOrder_WhenPayloadIsValid_Returns201AndOrderDTO() throws Exception {

        when(orderService.createOrder(any(CreateOrderDTO.class))).thenReturn(orderDTO);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrderDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.products[0].id").value(99))
                .andExpect(jsonPath("$.description").value("Test Order"))
                .andExpect(jsonPath("$.customer.name").value("John Doe"));
    }

    @Test
    void createOrder_WhenCustomerIdMissing_Returns400AndValidationMessage() throws Exception {
        CreateOrderDTO invalidInputDto = new CreateOrderDTO();
        invalidInputDto.setCustomerId(null);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidInputDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Input validation failed"))
                .andExpect(jsonPath("$.validationErrors.customerId").value("Customer ID is required"));
    }

    @Test
    void createOrder_WhenProductIdsCollectionIsEmpty_Returns400AndValidationMessage() throws Exception {
        CreateOrderDTO invalidInputDto = new CreateOrderDTO();
        invalidInputDto.setCustomerId(1L);
        invalidInputDto.setProductIds(Collections.emptyList());

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidInputDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.productIds")
                        .value("An order must contain at least one product ID"));
    }

    @Test
    void getOrderById_WhenOrderExists_ReturnsPopulatedProducts() throws Exception {
        Long targetId = 1L;
        when(orderService.getOrderById(targetId)).thenReturn(orderDTO);

        mockMvc.perform(get("/orders/{id}", targetId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(targetId))
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products[0].id").value(99))
                .andExpect(jsonPath("$.products[0].description").value("Logitech Mouse"));
    }

    @Test
    void getOrder_WhenUnexpectedErrorOccurs_Returns500FromGlobalHandler() throws Exception {
        Long targetId = 55L;

        when(orderService.getOrderById(targetId)).thenThrow(new RuntimeException("Severe database cluster failure"));

        mockMvc.perform(get("/orders/{id}", targetId).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError()) // Asserts HTTP 500
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred on the server."));
    }
}
