package com.example.store.integration;

import com.example.store.dto.CreateCustomerDTO;
import com.example.store.dto.CustomerDTO;
import com.example.store.entity.Customer;
import com.example.store.repository.CustomerRepository;
import com.example.store.repository.OrderRepository;
import com.example.store.repository.ProductRepository;
import com.example.store.service.CustomerService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        customerRepository.deleteAll();

        saveCustomer("Johnathan Miller");
        saveCustomer("Alice Smith");
        saveCustomer("Bongani Millard");
        saveCustomer("Thandiwe Nkosi");
    }

    private void saveCustomer(String name) {
        Customer customer = new Customer();
        customer.setName(name);
        customerRepository.save(customer);
    }

    private List<String> searchNames(String query) {
        return customerService.searchCustomers(query).stream()
                .map(CustomerDTO::getName)
                .sorted()
                .toList();
    }

    @Test
    void createAndSearchCustomer_EndToEndWorkflow() {
        CreateCustomerDTO newCustomer = new CreateCustomerDTO();
        newCustomer.setName("Priya Naidoo");

        CustomerDTO saved = customerService.createCustomer(newCustomer);
        assertNotNull(saved.getId());

        List<CustomerDTO> results = customerService.searchCustomers("naidoo");

        assertEquals(1, results.size());
        assertEquals("Priya Naidoo", results.get(0).getName());
        assertEquals(saved.getId(), results.get(0).getId());
    }

    @Test
    void searchCustomers_WhenQueryIsLowercase_MatchesStoredMixedCase() {
        assertEquals(List.of("Alice Smith"), searchNames("alice"));
    }

    @Test
    void searchCustomers_WhenQueryIsUppercase_MatchesStoredMixedCase() {
        assertEquals(List.of("Alice Smith"), searchNames("ALICE"));
    }

    @Test
    void searchCustomers_MatchesSubstringInTheMiddleOfAWord() {
        // "than" appears inside "Johnathan" but at the start of no word in that name.
        assertEquals(List.of("Johnathan Miller"), searchNames("than"));
    }

    @Test
    void searchCustomers_MatchesAgainstAnyWordInTheName() {
        // The task specifies matching a substring of one of the words, not just the first.
        assertEquals(List.of("Thandiwe Nkosi"), searchNames("nkosi"));
    }

    @Test
    void searchCustomers_WhenSubstringSpansMultipleCustomers_ReturnsAllOfThem() {
        assertEquals(List.of("Bongani Millard", "Johnathan Miller"), searchNames("mill"));
    }

    @Test
    void searchCustomers_WhenQueryIsShorterThanTheTrigramThreshold_StillReturnsCorrectResults() {
        // Patterns under 3 characters cannot use the GIN index and fall back to a
        // sequential scan. The plan differs; the result must not.
        assertEquals(List.of("Bongani Millard", "Johnathan Miller"), searchNames("ll"));
    }

    @Test
    void searchCustomers_WhenQueryIsPaddedWithWhitespace_IsStrippedBeforeMatching() {
        assertEquals(List.of("Alice Smith"), searchNames("   smith   "));
    }

    @Test
    void searchCustomers_WhenNothingMatches_ReturnsEmptyList() {
        List<CustomerDTO> results = customerService.searchCustomers("zzzznotacustomer");

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void searchCustomers_WhenQueryIsNull_ReturnsEveryCustomer() {
        assertEquals(4, customerService.searchCustomers(null).size());
    }

    @Test
    void searchCustomers_WhenQueryIsBlank_ReturnsEveryCustomer() {
        assertEquals(4, customerService.searchCustomers("   ").size());
    }
}
