package com.example.task.controller;

import com.example.task.Security.RateLimiterCust;
import com.example.task.Service.CustomerSer;
import com.example.task.model.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
@CrossOrigin(origins = "http://localhost:3000")
public class ControllerCust {

    private static final String VALID_API_KEY = "Cafe@123";
    @Autowired
    private CustomerSer cus;
    @Autowired
    private RateLimiterCust rateLimiter;

    // Find all customers with pagination, sorting, and filtering
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<Customer> findAll(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String order) {

        return cus.findCustomers(page, size, sortBy, order);
    }

    @GetMapping("/first-name/{firstName}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Customer> findByFirstName(@PathVariable String firstName) {
        return cus.findCustomerByFirstName(firstName);
    }

    @GetMapping("/last-name/{lastName}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Customer> findByLastName(@PathVariable String lastName) {
        return cus.findCustomerByLastName(lastName);
    }

    @GetMapping("/rating/{rating}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Customer> findByRatings(@PathVariable int rating) {
        return cus.findCustomersByRatings(rating);
    }

    @GetMapping("/feedback/{feedback}")
    public List<Customer> findCustomerByFeedback(@PathVariable String feedback) {
        return cus.findCustomerByFeedback(feedback);
    }

    @PutMapping("/update/{firstName}")
    public ResponseEntity<String> updateDetails(@PathVariable String firstName, @RequestBody Customer customer) {
        customer.setFirstName(firstName);
        Customer updatedCustomer=cus.updateCustomerDetails(customer.getId(), customer.getFirstName(), customer.getLastName());
        return new ResponseEntity<>("Customer updated successfully!", HttpStatus.OK);
    }
    @PutMapping("/update/{firstName}/feedback")
    public ResponseEntity<String> updateCustomerDetails(@PathVariable String firstName, @RequestBody Customer customer) {
        cus.updateCustomerFeedback(customer.getFeedback(), firstName);
        return new ResponseEntity<>("Customer feedback updated successfully!", HttpStatus.OK);
    }
    @PostMapping("/deleteSelected")
    public ResponseEntity<String> deleteSelectedCustomers(@RequestBody List<Long> customerIds) {
        for (Long id : customerIds) {
            cus.deleteCustomerByIdSoft(id);  // Call the soft delete for each selected ID
        }
        return ResponseEntity.ok("Selected customers have been marked as deleted.");
    }
    @PostMapping
    public ResponseEntity<String> createCustomer(@RequestHeader(value = "x-api-key", required = false) String apiKey,
                                                 @RequestBody Customer customer) {
         if (!rateLimiter.tryConsume(apiKey)) {
            return ResponseEntity.status(429).body("Rate limit exceeded. Try again later.");
        }
        cus.saveCustomers(List.of(customer));
        return new ResponseEntity<>("Customer created successfully!", HttpStatus.CREATED);

    }
    @PostMapping("/bulk")
    public ResponseEntity<String> createCustomers(@RequestBody List<Customer> customers) {
        cus.saveCustomers(customers);  // This method now handles saving multiple customers
        return new ResponseEntity<>("Customers registered successfully!", HttpStatus.CREATED);
    }
    // Search customers by name
    @GetMapping("/search")
    public List<Customer> searchCustomers(@RequestParam String name) {
        return cus.searchCustomersByName(name);
    }
}
