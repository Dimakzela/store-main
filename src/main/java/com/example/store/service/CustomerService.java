package com.example.store.service;

import com.example.store.dto.CreateCustomerDTO;
import com.example.store.dto.CustomerDTO;
import com.example.store.mapper.CustomerMapper;
import com.example.store.repository.CustomerRepository;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Transactional(readOnly = true)
    public List<CustomerDTO> searchCustomers(String query) {
        if (query == null || query.isBlank()) {
            return customerMapper.customersToCustomerDTOs(customerRepository.findAll());
        }

        return customerMapper.customersToCustomerDTOs(customerRepository.searchByNameSubstring(query.strip()));
    }

    @Transactional
    public CustomerDTO createCustomer(CreateCustomerDTO customer) {
        return customerMapper.customerToCustomerDTO(
                customerRepository.save(customerMapper.createDTOToCustomer(customer)));
    }
}
