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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderMapper orderMapper;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, customerRepository, productRepository, orderMapper);
    }

    @Test
    void getAllOrders_WhenEntitiesExist_ReturnsMappedDtoList() {
        Order order1 = new Order();
        order1.setId(10L);
        order1.setDescription("First Test Order");

        Order order2 = new Order();
        order2.setId(20L);
        order2.setDescription("Second Test Order");

        List<Order> mockOrders = List.of(order1, order2);

        OrderDTO dto1 = new OrderDTO();
        dto1.setId(10L);
        dto1.setDescription("First Test Order");

        OrderDTO dto2 = new OrderDTO();
        dto2.setId(20L);
        dto2.setDescription("Second Test Order");

        when(orderRepository.findAll()).thenReturn(mockOrders);
        when(orderMapper.ordersToOrderDTOs(List.of(order1, order2))).thenReturn(List.of(dto1, dto2));

        List<OrderDTO> result = orderService.getAllOrders();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(10L, result.get(0).getId());
        assertEquals("First Test Order", result.get(0).getDescription());
        assertEquals(20L, result.get(1).getId());
        assertEquals("Second Test Order", result.get(1).getDescription());

        verify(orderRepository, times(1)).findAll();
        verify(orderMapper, times(1)).ordersToOrderDTOs(List.of(order1, order2));
    }

    @Test
    void getAllOrders_WhenNoEntitiesExist_ReturnsEmptyList() {
        when(orderRepository.findAll()).thenReturn(List.of());

        List<OrderDTO> result = orderService.getAllOrders();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(orderRepository, times(1)).findAll();
        verify(orderMapper, never()).orderToOrderDTO(any());
    }

    @Test
    void getOrderById_WhenOrderExists_ReturnsOrderDTO() {
        Long orderId = 1L;
        Order mockOrder = mock(Order.class);
        OrderDTO mockOrderDTO = mock(OrderDTO.class);

        when(orderRepository.findWithCustomerAndProductsById(orderId)).thenReturn(Optional.of(mockOrder));
        when(orderMapper.orderToOrderDTO(mockOrder)).thenReturn(mockOrderDTO);

        OrderDTO result = orderService.getOrderById(orderId);

        assertNotNull(result);
        assertEquals(mockOrderDTO, result);

        verify(orderRepository, times(1)).findWithCustomerAndProductsById(orderId);
        verify(orderMapper, times(1)).orderToOrderDTO(mockOrder);
    }

    @Test
    void getOrderById_WhenOrderDoesNotExist_ThrowsNotFoundException() {
        Long orderId = 99L;
        when(orderRepository.findWithCustomerAndProductsById(orderId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> orderService.getOrderById(orderId));

        assertTrue(exception.getMessage().contains("Order"));
        assertTrue(exception.getMessage().contains(orderId.toString()));

        verifyNoInteractions(orderMapper);
    }

    @Test
    void createOrder_WithValidInput_ReturnsCreatedOrderDTO() {
        Long customerId = 1L;
        List<Long> productIds = List.of(99L, 100L);

        CreateOrderDTO inputDto = new CreateOrderDTO();
        inputDto.setCustomerId(customerId);
        inputDto.setDescription("Test Order");
        inputDto.setProductIds(productIds);

        Customer mockCustomer = new Customer();
        mockCustomer.setId(customerId);

        Product p1 = new Product();
        p1.setId(99L);
        Product p2 = new Product();
        p2.setId(100L);
        List<Product> mockProducts = List.of(p1, p2);

        Order savedOrder = new Order();
        savedOrder.setId(500L);

        OrderDTO expectedOutboundDto = new OrderDTO();
        expectedOutboundDto.setId(500L);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(mockCustomer));
        when(productRepository.findAllById(productIds)).thenReturn(mockProducts);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderMapper.orderToOrderDTO(savedOrder)).thenReturn(expectedOutboundDto);

        OrderDTO result = orderService.createOrder(inputDto);

        assertNotNull(result);
        assertEquals(expectedOutboundDto, result);

        verify(customerRepository, times(1)).findById(customerId);
        verify(productRepository, times(1)).findAllById(productIds);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderMapper, times(1)).orderToOrderDTO(savedOrder);
    }

    @Test
    void createOrder_WhenDatabaseThrowsException_PropagatesException() {
        Long customerId = 1L;
        List<Long> productIds = List.of(99L);

        CreateOrderDTO inputDto = new CreateOrderDTO();
        inputDto.setCustomerId(customerId);
        inputDto.setProductIds(productIds);
        inputDto.setDescription("Timeout Test Order");

        Customer mockCustomer = new Customer();
        mockCustomer.setId(customerId);

        Product mockProduct = new Product();
        mockProduct.setId(99L);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(mockCustomer));
        when(productRepository.findAllById(productIds)).thenReturn(List.of(mockProduct));

        when(orderRepository.save(any(Order.class))).thenThrow(new RuntimeException("Database connection timeout"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.createOrder(inputDto);
        });

        assertEquals("Database connection timeout", exception.getMessage());

        verify(orderRepository, times(1)).save(any(Order.class));
        verifyNoInteractions(orderMapper);
    }

    @Test
    void createOrder_WhenProductIdsAreInvalid_ThrowsIllegalArgumentException() {
        Long customerId = 1L;
        List<Long> productIds = List.of(99L, 100L);

        CreateOrderDTO inputDto = new CreateOrderDTO();
        inputDto.setCustomerId(customerId);
        inputDto.setProductIds(productIds);

        Customer mockCustomer = new Customer();

        Product p1 = new Product();
        p1.setId(99L);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(mockCustomer));
        when(productRepository.findAllById(productIds)).thenReturn(List.of(p1));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            orderService.createOrder(inputDto);
        });

        assertEquals("One or more product IDs provided are invalid", exception.getMessage());

        verify(orderRepository, never()).save(any());
        verifyNoInteractions(orderMapper);
    }
}
