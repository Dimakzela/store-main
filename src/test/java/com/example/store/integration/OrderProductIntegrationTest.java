package com.example.store.integration;

import com.example.store.dto.CreateOrderDTO;
import com.example.store.dto.OrderDTO;
import com.example.store.entity.Customer;
import com.example.store.entity.Order;
import com.example.store.entity.Product;
import com.example.store.repository.CustomerRepository;
import com.example.store.repository.OrderRepository;
import com.example.store.repository.ProductRepository;
import com.example.store.service.OrderService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderProductIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    private Customer savedCustomer;

    @BeforeEach
    void setUp() {
        // Clear children and join mapping nodes first to prevent FK constraint drops
        orderRepository.deleteAll();
        productRepository.deleteAll();
        customerRepository.deleteAll();

        // 1. Establish an active customer parent row in our live PostgreSQL container
        Customer customer = new Customer();
        customer.setName("Sipho Modise");
        savedCustomer = customerRepository.save(customer);
    }

    @Test
    void orderAndProduct_ManyToManyLifecycleTest() {
        // 2. Arrange: Populate standalone inventory catalog records inside your database cluster
        Product product1 = new Product();
        product1.setDescription("4K Curved Monitor");

        Product product2 = new Product();
        product2.setDescription("Ergonomic Chair");

        List<Product> savedProducts = productRepository.saveAll(List.of(product1, product2));

        List<Long> savedProductIds = savedProducts.stream().map(Product::getId).toList();

        // 3. Arrange: Build input request structures for a fresh checkout transaction
        CreateOrderDTO createOrderDTO = new CreateOrderDTO();
        createOrderDTO.setCustomerId(savedCustomer.getId());
        createOrderDTO.setProductIds(savedProductIds);
        createOrderDTO.setDescription("Premium Home Office Setup");

        // 4. Act: Persist the base order record
        OrderDTO savedOrderDTO = orderService.createOrder(createOrderDTO);
        assertNotNull(savedOrderDTO.getId());

        // 5. Act: Connect the products to the order to populate the 'order_product' join table
        Order liveOrderEntity = orderRepository.findById(savedOrderDTO.getId()).orElseThrow();
        liveOrderEntity.setProducts(savedProducts); // Attaches our catalog items to this order entry
        orderRepository.save(liveOrderEntity); // Saves the relationship link

        // 6. Act: Execute a complete fetch-all routine to test list processing over the high-latency link
        List<OrderDTO> allOrders = orderService.getAllOrders();

        // 7. Assert: Validate that our payload contains the nested items cleanly
        assertNotNull(allOrders);
        assertEquals(1, allOrders.size());

        OrderDTO retrievedOrder = allOrders.get(0);
        assertEquals(savedOrderDTO.getId(), retrievedOrder.getId());
        assertEquals("Sipho Modise", retrievedOrder.getCustomer().getName());

        // Assert children products array mapped through cleanly from the junction mapping database views
        assertNotNull(retrievedOrder.getProducts());
        assertEquals(2, retrievedOrder.getProducts().size());

        boolean hasMonitor = retrievedOrder.getProducts().stream()
                .anyMatch(p -> p.getDescription().equals("4K Curved Monitor"));
        boolean hasChair = retrievedOrder.getProducts().stream()
                .anyMatch(p -> p.getDescription().equals("Ergonomic Chair"));

        assertTrue(
                hasMonitor, "The order payload response should map and return the contained 4K Curved Monitor record");
        assertTrue(hasChair, "The order payload response should map and return the contained Ergonomic Chair record");
    }
}
