package com.example.store.service;

import com.example.store.dto.CreateCustomerDTO;
import com.example.store.dto.CustomerDTO;
import com.example.store.entity.Customer;
import com.example.store.mapper.CustomerMapper;
import com.example.store.repository.CustomerRepository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(customerRepository, customerMapper);
    }

    @Test
    void getAllCustomers_WhenEntitiesExist_ReturnsMappedDtoList() {
        Customer customer1 = new Customer();
        Customer customer2 = new Customer();
        List<Customer> mockCustomers = List.of(customer1, customer2);

        CustomerDTO dto1 = new CustomerDTO();
        CustomerDTO dto2 = new CustomerDTO();
        List<CustomerDTO> expectedDtos = List.of(dto1, dto2);

        when(customerRepository.findAll()).thenReturn(mockCustomers);
        when(customerMapper.customersToCustomerDTOs(mockCustomers)).thenReturn(expectedDtos);

        List<CustomerDTO> result = customerService.searchCustomers(null);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedDtos, result);

        verify(customerRepository, times(1)).findAll();
        verify(customerMapper, times(1)).customersToCustomerDTOs(mockCustomers);
    }

    @Test
    void createCustomer_WithValidInput_ReturnsCreatedCustomerDTO() {
        CreateCustomerDTO inputDto = new CreateCustomerDTO();
        inputDto.setName("Jane Doe");

        Customer unmappedCustomer = new Customer();
        Customer savedCustomer = new Customer();
        savedCustomer.setId(1L);
        savedCustomer.setName("Jane Doe");

        CustomerDTO expectedOutboundDto = new CustomerDTO();
        expectedOutboundDto.setId(1L);
        expectedOutboundDto.setName("Jane Doe");

        when(customerMapper.createDTOToCustomer(inputDto)).thenReturn(unmappedCustomer);
        when(customerRepository.save(unmappedCustomer)).thenReturn(savedCustomer);
        when(customerMapper.customerToCustomerDTO(savedCustomer)).thenReturn(expectedOutboundDto);

        CustomerDTO result = customerService.createCustomer(inputDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Jane Doe", result.getName());
        assertEquals(expectedOutboundDto, result);

        verify(customerMapper, times(1)).createDTOToCustomer(inputDto);
        verify(customerRepository, times(1)).save(unmappedCustomer);
        verify(customerMapper, times(1)).customerToCustomerDTO(savedCustomer);
    }

    @Test
    void createCustomer_WhenDatabaseThrowsException_PropagatesError() {
        CreateCustomerDTO inputDto = new CreateCustomerDTO();
        Customer unmappedCustomer = new Customer();

        when(customerMapper.createDTOToCustomer(inputDto)).thenReturn(unmappedCustomer);
        when(customerRepository.save(unmappedCustomer))
                .thenThrow(new RuntimeException("Database unique constraint violation"));

        RuntimeException exception =
                assertThrows(RuntimeException.class, () -> customerService.createCustomer(inputDto));

        assertEquals("Database unique constraint violation", exception.getMessage());

        verify(customerMapper, never()).customerToCustomerDTO(any());
    }

    @Test
    void searchCustomers_WhenQueryIsNull_ReturnsAllCustomers() {
        Customer customer1 = new Customer();
        Customer customer2 = new Customer();
        List<Customer> mockCustomers = List.of(customer1, customer2);

        CustomerDTO dto1 = new CustomerDTO();
        CustomerDTO dto2 = new CustomerDTO();
        List<CustomerDTO> expectedDtos = List.of(dto1, dto2);

        when(customerRepository.findAll()).thenReturn(mockCustomers);
        when(customerMapper.customersToCustomerDTOs(mockCustomers)).thenReturn(expectedDtos);

        List<CustomerDTO> result = customerService.searchCustomers(null);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedDtos, result);

        verify(customerRepository, times(1)).findAll();
        verify(customerRepository, never()).searchByNameSubstring(any());
    }

    @Test
    void searchCustomers_WhenQueryIsBlank_ReturnsAllCustomers() {
        Customer customer = new Customer();
        CustomerDTO dto = new CustomerDTO();

        when(customerRepository.findAll()).thenReturn(List.of(customer));
        when(customerMapper.customersToCustomerDTOs(anyList())).thenReturn(List.of(dto));

        List<CustomerDTO> result = customerService.searchCustomers("   ");

        assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());

        verify(customerRepository, times(1)).findAll();
        verify(customerRepository, never()).searchByNameSubstring(any());
    }

    @Test
    void searchCustomers_WhenQueryIsValid_ReturnsFilteredCustomers() {
        String validQuery = "ohn";

        Customer matchingCustomer = new Customer();
        matchingCustomer.setName("John Doe");
        List<Customer> mockResultList = List.of(matchingCustomer);

        CustomerDTO expectedDto = new CustomerDTO();
        expectedDto.setName("John Doe");
        List<CustomerDTO> expectedMappedList = List.of(expectedDto);

        when(customerRepository.searchByNameSubstring("ohn")).thenReturn(mockResultList);
        when(customerMapper.customersToCustomerDTOs(mockResultList)).thenReturn(expectedMappedList);

        List<CustomerDTO> result = customerService.searchCustomers(validQuery);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getName());

        verify(customerRepository, times(1)).searchByNameSubstring("ohn");
        verify(customerRepository, never()).findAll();
    }
}
