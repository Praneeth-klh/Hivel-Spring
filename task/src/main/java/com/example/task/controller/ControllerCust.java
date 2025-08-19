package com.example.task.controller;

import com.example.task.Service.CustomerSer;
import com.example.task.model.Customer;
import com.example.task.Security.RateLimiterCust;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public Page<Customer> findAll(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(required = false) String filterBy,
            @RequestParam(required = false) String filterValue) {
        return cus.findCustomers(page, size, sortBy, order, filterBy, filterValue);
    }

    // Find customers by first name
    @GetMapping("/first-name/{firstName}")
    public List<Customer> findByFirstName(@PathVariable String firstName) {
        return cus.findCustomerByFirstName(firstName);
    }

    // Find customers by last name
    @GetMapping("/last-name/{lastName}")
    public List<Customer> findByLastName(@PathVariable String lastName) {
        return cus.findCustomerByLastName(lastName);
    }

    // Find customers by ratings
    @GetMapping("/rating/{rating}")
    public List<Customer> findByRatings(@PathVariable int rating) {
        return cus.findCustomersByRatings(rating);
    }

    // Find customers by ID
    @GetMapping("/Id/{Id}")
    public List<Customer> findById(@PathVariable int id) {
        return cus.findById(id);
    }

    // Find customers by feedback
    @GetMapping("/feedback/{feedback}")
    public List<Customer> findCustomerByFeedback(@PathVariable String feedback) {
        return cus.findCustomerByFeedback(feedback);
    }

    // Restore soft-deleted admins
    @GetMapping("/restore/admins")
    public ResponseEntity<List<Customer>> restoreSoftDeletedAdmins() {
        List<Customer> restoredCustomers = cus.restoreSoftDeletedAdmins();
        if (restoredCustomers.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(restoredCustomers);
    }

    // Update customer details
    @PutMapping("/update/{firstName}")
    public ResponseEntity<String> updateDetails(@PathVariable String firstName, @RequestBody Customer customer) {
        cus.updateCustomerDetails(customer.getId(), customer.getFirstName(), customer.getLastName());
        return new ResponseEntity<>("Customer updated successfully!", HttpStatus.OK);
    }

    // Update customer feedback
    @PutMapping("/update/{firstName}/feedback")
    public ResponseEntity<String> updateCustomerDetails(@PathVariable String firstName, @RequestBody Customer customer) {
        cus.updateCustomerFeedback(customer.getFeedback(), firstName);
        return new ResponseEntity<>("Customer feedback updated successfully!", HttpStatus.OK);
    }

    // Soft delete selected customers
    @PostMapping("/deleteSelected")
    public ResponseEntity<String> deleteSelectedCustomers(@RequestBody List<Long> customerIds) {
        for (Long id : customerIds) {
            cus.deleteCustomerByIdSoft(id);
        }
        return ResponseEntity.ok("Selected customers have been marked as deleted.");
    }

    // Create a new customer
    @PostMapping
    public ResponseEntity<String> createCustomer(@RequestHeader(value = "x-api-key", required = false) String apiKey, @RequestBody Customer customer) {
        if (apiKey == null || apiKey.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("API Key is missing. Please provide a valid API key.");
        }
        if (!rateLimiter.tryConsume(apiKey)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Rate limit exceeded. Try again later.");
        }
        cus.saveCustomers(List.of(customer));
        return new ResponseEntity<>("Customer created successfully!", HttpStatus.CREATED);
    }

    // Create multiple customers
    @PostMapping("/bulk")
    public ResponseEntity<String> createCustomers(@RequestBody List<Customer> customers) {
        cus.saveCustomers(customers);
        return new ResponseEntity<>("Customers registered successfully!", HttpStatus.CREATED);
    }

    // Search customers by name
    @GetMapping("/search")
    public List<Customer> searchCustomers(@RequestParam String name) {
        return cus.searchCustomersByName(name);
    }
}
