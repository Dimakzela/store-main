package com.example.store.integration;

import com.example.store.dto.CreateOrderDTO;
import com.example.store.dto.OrderDTO;
import com.example.store.dto.ProductDTO;
import com.example.store.entity.Customer;
import com.example.store.entity.Product;
import com.example.store.repository.CustomerRepository;
import com.example.store.repository.OrderRepository;
import com.example.store.repository.ProductRepository;
import com.example.store.service.OrderService;
import com.example.store.service.ProductService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exercises the product read paths against a live PostgreSQL container.
 *
 * <p>These reads rely on {@code @EntityGraph(attributePaths = "orders")}, and an entity graph naming a non-existent
 * attribute only fails when the graph is resolved at query time - never in a mock-based unit test. This suite is the
 * guard for that class of regression.
 */
class ProductIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    private Product monitor;
    private Product chair;
    private Long orderId;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        customerRepository.deleteAll();

        Customer customer = new Customer();
        customer.setName("Sipho Modise");
        Customer savedCustomer = customerRepository.save(customer);

        Product product1 = new Product();
        product1.setDescription("4K Curved Monitor");

        Product product2 = new Product();
        product2.setDescription("Ergonomic Chair");

        List<Product> saved = productRepository.saveAll(List.of(product1, product2));
        monitor = saved.get(0);
        chair = saved.get(1);

        CreateOrderDTO createOrderDTO = new CreateOrderDTO();
        createOrderDTO.setCustomerId(savedCustomer.getId());
        createOrderDTO.setDescription("Premium Home Office Setup");
        createOrderDTO.setProductIds(List.of(monitor.getId()));

        OrderDTO savedOrder = orderService.createOrder(createOrderDTO);
        orderId = savedOrder.getId();
    }

    @Test
    void getAllProducts_resolvesOrdersEntityGraphAndMapsOrderIds() {
        List<ProductDTO> products = productService.getAllProducts();

        assertEquals(2, products.size());

        ProductDTO monitorDTO = products.stream()
                .filter(p -> p.getId().equals(monitor.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals(List.of(orderId), monitorDTO.getOrderIds());

        ProductDTO chairDTO = products.stream()
                .filter(p -> p.getId().equals(chair.getId()))
                .findFirst()
                .orElseThrow();
        assertTrue(chairDTO.getOrderIds().isEmpty(), "A product in no orders should map to an empty orderIds array");
    }

    @Test
    void getProductById_resolvesOrdersEntityGraphAndMapsOrderIds() {
        ProductDTO product = productService.getProductById(monitor.getId());

        assertEquals(monitor.getId(), product.getId());
        assertEquals("4K Curved Monitor", product.getDescription());
        assertEquals(List.of(orderId), product.getOrderIds());
    }
}
