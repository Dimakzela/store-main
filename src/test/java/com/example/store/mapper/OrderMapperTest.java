package com.example.store.mapper;

import com.example.store.dto.OrderDTO;
import com.example.store.dto.OrderProductDTO;
import com.example.store.entity.Customer;
import com.example.store.entity.Order;
import com.example.store.entity.Product;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderMapperTest {

    private final OrderMapper orderMapper = new OrderMapperImpl();

    @Test
    void orderToOrderDTO_WithFullEntityGraph_MapsSuccessfully() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("John Doe");

        Product product1 = new Product();
        product1.setId(101L);
        product1.setDescription("Wireless Mouse");

        Product product2 = new Product();
        product2.setId(102L);
        product2.setDescription("Mechanical Keyboard");

        Order order = new Order();
        order.setId(55L);
        order.setDescription("Office Upgrade Order");
        order.setCustomer(customer);
        order.setProducts(List.of(product1, product2));

        OrderDTO result = orderMapper.orderToOrderDTO(order);

        assertNotNull(result);
        assertEquals(55L, result.getId());
        assertEquals("Office Upgrade Order", result.getDescription());

        // Assert Customer Details
        assertNotNull(result.getCustomer());
        assertEquals(1L, result.getCustomer().getId());
        assertEquals("John Doe", result.getCustomer().getName());

        // Assert Nested Products Array
        assertNotNull(result.getProducts());
        assertEquals(2, result.getProducts().size());

        OrderProductDTO mappedProduct1 = result.getProducts().get(0);
        assertEquals(101L, mappedProduct1.getId());
        assertEquals("Wireless Mouse", mappedProduct1.getDescription());

        OrderProductDTO mappedProduct2 = result.getProducts().get(1);
        assertEquals(102L, mappedProduct2.getId());
        assertEquals("Mechanical Keyboard", mappedProduct2.getDescription());
    }
}
