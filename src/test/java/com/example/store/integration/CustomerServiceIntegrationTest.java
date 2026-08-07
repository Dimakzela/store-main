package com.example.store.integration;

import com.example.store.dto.CreateCustomerDTO;
import com.example.store.dto.CustomerDTO;
import com.example.store.service.CustomerService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomerServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CustomerService customerService;

    @Test
    void createAndSearchCustomer_EndToEndWorkflow() {
        CreateCustomerDTO customer1 = new CreateCustomerDTO();
        customer1.setName("Alice Smith");

        CreateCustomerDTO customer2 = new CreateCustomerDTO();
        customer2.setName("Johnathan Miller");

        CustomerDTO saved1 = customerService.createCustomer(customer1);
        CustomerDTO saved2 = customerService.createCustomer(customer2);

        assertNotNull(saved1.getId());
        assertNotNull(saved2.getId());

        List<CustomerDTO> searchResults = customerService.searchCustomers("miller");

        assertEquals(1, searchResults.size());
        assertEquals("Johnathan Miller", searchResults.get(0).getName());
        assertEquals(saved2.getId(), searchResults.get(0).getId());
    }
}
