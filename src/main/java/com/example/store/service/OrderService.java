package com.example.store.service;

import com.example.store.dto.CreateOrderDTO;
import com.example.store.dto.OrderDTO;
import com.example.store.entity.Customer;
import com.example.store.entity.Order;
import com.example.store.entity.Product;
import com.example.store.exception.NotFoundException;
import com.example.store.mapper.OrderMapper;
import com.example.store.repository.CustomerRepository;
import com.example.store.repository.OrderRepository;
import com.example.store.repository.ProductRepository;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;

    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {
        return orderMapper.ordersToOrderDTOs(orderRepository.findAll());
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long id) {
        return orderRepository
                .findWithCustomerAndProductsById(id)
                .map(orderMapper::orderToOrderDTO)
                .orElseThrow(() -> new NotFoundException("Order", id));
    }

    @Transactional
    public OrderDTO createOrder(@Valid CreateOrderDTO dto) {
        // 1. Find the active customer row
        Customer customer = customerRepository
                .findById(dto.getCustomerId())
                .orElseThrow(() -> new NotFoundException("Customer", dto.getCustomerId()));

        // 2. Find all requested catalog product rows at once
        List<Product> products = productRepository.findAllById(dto.getProductIds());
        if (products.isEmpty() || products.size() != dto.getProductIds().size()) {
            throw new IllegalArgumentException("One or more product IDs provided are invalid");
        }

        // 3. Construct and link the order entity
        Order order = new Order();
        order.setDescription(dto.getDescription());
        order.setCustomer(customer);
        order.setProducts(products); // <-- This triggers the entries into the 'order_product' join table!

        // 4. Save and return outbound DTO
        Order savedOrder = orderRepository.save(order);
        return orderMapper.orderToOrderDTO(savedOrder);
    }
}
