package com.example.task;

import com.example.task.Service.CustomerSer;
import com.example.task.model.Customer;
import com.example.task.repository.Customerepo;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CustomerSerTest {

    @Autowired
    private CustomerSer customerSer;

    @Autowired
    private Customerepo customerRepo;
    @Test
    public void testSaveCustomer() {
        // Creating a new customer
        Customer customer = new Customer();
        customer.setFirstName("John");
        customer.setLastName("Doe");

        customer.setRating(4);
        customer.setFeedback("Good service");

        // Save the customer
        customerSer.saveCustomer(customer);

        // Verifying customer is saved
        Optional<Customer> savedCustomer = customerRepo.findById(customer.getId());
        assertTrue(savedCustomer.isPresent());
        assertEquals("John", savedCustomer.get().getFirstName());
        assertEquals("Doe", savedCustomer.get().getLastName());
    }

    @Test
    public void testFindCustomerById() {
        // Assuming a customer exists in DB
        Customer customer = customerRepo.findById(1L).orElseThrow();
        assertNotNull(customer);
    }

    @Test
    public void testFindCustomersByFirstName() {
        // Assuming a customer with this first name exists
        List<Customer> customers = customerSer.findCustomerByFirstName("John");
        assertFalse(customers.isEmpty());
    }

    @Test
    public void testUpdateCustomerDetails() {
        // Assuming a customer with ID 1 exists
        Customer customer = customerRepo.findById(1L).orElseThrow();
        customer.setFirstName("Johnny");
        customerSer.saveCustomer(customer);

        // Verify update
        Customer updatedCustomer = customerRepo.findById(1L).orElseThrow();
        assertEquals("Johnny", updatedCustomer.getFirstName());
    }
    @Test
    public void testDeleteCustomer() {
        // Given: mock the repository to return a customer with ID 1 that is not deleted
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setDeleted(false);  // Initially not deleted

        when(customerRepo.findById(1L)).thenReturn(Optional.of(customer));  // Mock repository response

        // When: Call the soft delete method
        boolean result = customerSer.deleteCustomerByIdSoft(1L);

        // Then: Verify that the method returns true (successful deletion)
        assertTrue(result);

        // Also, verify that the customer is now marked as deleted
        verify(customerRepo, times(1)).save(customer);  // Verify that the save method was called once
        assertTrue(customer.isDeleted());  // Make sure the customer is marked as deleted
    }
}
