package com.example.store.controller;

import com.example.store.dto.CreateCustomerDTO;
import com.example.store.dto.CustomerDTO;
import com.example.store.exception.GlobalExceptionHandler;
import com.example.store.mapper.CustomerMapper;
import com.example.store.service.CustomerService;
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

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
@Import(GlobalExceptionHandler.class)
@ComponentScan(basePackageClasses = {CustomerMapper.class})
class CustomerControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

    private CustomerDTO customerDto;

    @BeforeEach
    void setUp() {
        customerDto = new CustomerDTO();
        customerDto.setId(1L);
        customerDto.setName("John Doe");
    }

    @Test
    void getAllCustomers_Returns200AndOrderList() throws Exception {
        when(customerService.searchCustomers(null)).thenReturn(List.of(customerDto));

        mockMvc.perform(get("/customers").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("John Doe"));
    }

    @Test
    void getCustomers_WithSearchQuery_ReturnsFilteredList() throws Exception {
        CustomerDTO matchingCustomer = new CustomerDTO();
        matchingCustomer.setId(1L);
        matchingCustomer.setName("John Doe");

        when(customerService.searchCustomers("ohn")).thenReturn(List.of(matchingCustomer));

        mockMvc.perform(get("/customers").param("name", "ohn").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("John Doe"));
    }

    @Test
    void createCustomer_WhenPayloadIsValid_Returns201AndOrderDTO() throws Exception {
        CreateCustomerDTO inputDto = new CreateCustomerDTO();
        inputDto.setName("John Doe");

        when(customerService.createCustomer(any(CreateCustomerDTO.class))).thenReturn(customerDto);

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void createCustomer_WhenNameMissing_Returns400AndValidationMessage() throws Exception {
        CreateCustomerDTO invalidInputDto = new CreateCustomerDTO();
        when(customerService.createCustomer(any(CreateCustomerDTO.class))).thenReturn(customerDto);

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidInputDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Input validation failed"))
                .andExpect(jsonPath("$.validationErrors.name").value("Customer name is required"));
    }
}
