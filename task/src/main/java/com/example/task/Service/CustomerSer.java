package com.example.task.Service;

import com.example.task.Infrastructure.CacheManagers;
import com.example.task.model.Customer;
import com.example.task.model.CustomerAudits;
import com.example.task.repository.CustomerA;
import com.example.task.repository.Customerepo;
import com.example.task.repository.Paging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomerSer {

    @Autowired
    private Customerepo c;
    @Autowired
    private Paging p;
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private CustomerA customerAuditRepository;
    private static final Logger logger = LoggerFactory.getLogger(CustomerSer.class);

    // Sorting and Filtering
    @Cacheable(value = "customerCache", key = "#page + '-' + #size + '-' + #sortBy + '-' + #order + '-' + #filterBy + '-' + #filterValue")
    public Page<Customer> findCustomers(int page, int size, String sortBy, String order, String filterBy, String filterValue) {
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "id";
        }
        if (order == null || order.isEmpty()) {
            order = "asc";
        }

        Sort.Direction direction = order.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        // Filtering by field if provided
        if (filterBy != null && !filterBy.isEmpty() && filterValue != null && !filterValue.isEmpty()) {
            logger.info("Filtering by field: {} with value: {}", filterBy, filterValue);
            switch (filterBy.toLowerCase()) {
                case "firstname":
                    return c.findByFirstNameIgnoreCaseContainingAndIsDeletedFalse(filterValue, pageable);
                case "lastname":
                    return c.findByLastNameIgnoreCaseContainingAndIsDeletedFalse(filterValue, pageable);
                case "rating":
                    int rating = Integer.parseInt(filterValue);  // Assuming filterValue is a valid integer
                    return c.findByRatingGreaterThanEqualAndIsDeletedFalse(rating, pageable);
                case "feedback":
                    return c.findByFeedbackContainingIgnoreCaseAndIsDeletedFalse(filterValue, pageable);
                default:
                    logger.warn("Unknown filter field: {}", filterBy);
                    break;
            }
        }
        return c.findAllByIsDeletedFalse(pageable);  // Default query if no filter
    }

    // Find by ID
    @Cacheable(value = "customerCache", key = "#id")
    public List<Customer> findById(int id) {
        logger.info("Finding customer by ID: {}", id);
        return c.findById(id);
    }

    // Search customers by name
    public List<Customer> searchCustomersByName(String name) {
        List<Customer> allCustomers = (List<Customer>) cacheManager.getCache("customerCache").get("allCustomers").get();
        logger.info("Searching customers by name: {}", name);
        return allCustomers.stream()
                .filter(customer -> customer.getFirstName().toLowerCase().contains(name.toLowerCase()) ||
                        customer.getLastName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
    }

    // Update customer details
    @CacheEvict(value = "customerCache", key = "#id")
    public Customer updateCustomerDetails(Long id, String firstName, String lastName) {
        logger.info("Updating customer details for customer id: {}, firstName: {}, lastName: {}", id, firstName, lastName);
        Optional<Customer> customerOpt = c.findById(id);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            if (firstName != null && !firstName.isEmpty()) {
                customer.setFirstName(firstName);
            }
            if (lastName != null && !lastName.isEmpty()) {
                customer.setLastName(lastName);
            }
            logger.info("Updated customer details: {}", firstName);
            return c.save(customer);
        }
        logger.warn("Customer with id: {} not found", id);
        return null;
    }

    // Update customer feedback
    public Customer updateCustomerFeedback(String feedback, String firstName) {
        logger.info("Updating customer feedback for customer with firstName: {}", firstName);
        List<Customer> customers = c.findByFirstNameIgnoreCase(firstName);
        if (!customers.isEmpty()) {
            Customer customer = customers.get(0);
            if (feedback != null && !feedback.isEmpty()) {
                customer.setFeedback(feedback);
            }
            logger.info("Updated customer feedback: {}", feedback);
            return c.save(customer);
        }
        return null;
    }

    // Hard delete customer by ID
    public boolean deleteCustomerByIdHard(Long id) {
        Optional<Customer> optionalCustomer = c.findById(id);
        if (optionalCustomer.isPresent() && !optionalCustomer.get().isDeleted()) {
            c.deleteById(id);
            logger.info("Hard deleted customer with id: {}", id);
            return true;
        }
        logger.warn("Customer with id: {} not found or already deleted", id);
        return false;
    }

    // Soft delete customer by ID
    @CacheEvict(value = "customerCache", key = "#id")
    public boolean deleteCustomerByIdSoft(Long id) {
        Optional<Customer> optionalCustomer = c.findById(id);
        if (optionalCustomer.isPresent() && !optionalCustomer.get().isDeleted()) {
            optionalCustomer.get().setDeleted(true);
            logger.info("Soft deleted customer with id: {}", id);
            c.save(optionalCustomer.get());
            return true;
        }
        logger.warn("Customer with id: {} not found or already deleted", id);
        return false;
    }

    // Restore soft-deleted admins
    public List<Customer> restoreSoftDeletedAdmins() {
        logger.info("Restoring soft deleted customers");
        List<Customer> deletedAdminCustomers = c.findByIsDeletedTrue();
        deletedAdminCustomers.forEach(customer -> {
            customer.setUpdatedAt(LocalDateTime.now());
            customer.setUpdatedBy("admin");
            c.save(customer);
        });
        return deletedAdminCustomers;
    }

    // Save a customer
    public void saveCustomer(Customer customer) {
        logger.info("Saving customer with first name: {}", customer.getFirstName());
        c.save(customer);
    }

    // Save multiple customers
    public void saveCustomers(List<Customer> customers) {
        LocalDateTime currentTime = LocalDateTime.now();
        for (Customer customer : customers) {
            if (customer.getId() == null) {
                logger.info("Creating new customer: {}", customer.getFirstName());
                customer.setCreatedAt(currentTime);
                customer.setCreatedBy(customer.getFirstName());
            } else {
                logger.info("Updating customer: {}", customer.getFirstName());
            }
            customer.setUpdatedAt(currentTime);
            customer.setUpdatedBy(customer.getFirstName());
        }
        c.saveAll(customers);
        for (Customer customer : customers) {
            if (customer.getId() == null) {
                logCustomerAudit(customer, "created");
            } else {
                logCustomerAudit(customer, "updated");
            }
        }
    }

    // Log customer audit
    private void logCustomerAudit(Customer customer, String action) {
        CustomerAudits audit = new CustomerAudits(customer, action, LocalDateTime.now());
        logger.info("{} has sent into logs audits {}", customer.getFirstName(), action);
        customerAuditRepository.save(audit);
    }

    // Find customers by first name
    @Cacheable(value = "customerCache", key = "'firstName-'+#s", unless = "#result == null || #result.isEmpty()")
    public List<Customer> findCustomerByFirstName(String s) {
        logger.info("Finding customers by first name: {}", s);
        if (s == null || s.trim().isEmpty()) {
            logger.error("First name cannot be null or empty");
            throw new IllegalArgumentException("First name cannot be null or empty");
        }
        List<Customer> customers = c.findByFirstNameIgnoreCase(s.trim());
        customers.removeIf(Customer::isDeleted);
        logger.info("{} customers found with first name {}", customers.size(), s);
        return customers;
    }

    // Find customers by last name
    @Cacheable(value = "customerCache", key = "'lastName-'+#lastName", unless = "#result == null || #result.isEmpty()")
    public List<Customer> findCustomerByLastName(String lastName) {
        logger.info("Finding customers by last name: {}", lastName);
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be null or empty");
        }
        List<Customer> customers = c.findByLastNameIgnoreCase(lastName.trim());
        customers.removeIf(Customer::isDeleted);
        return customers;
    }

    // Find customers by feedback
    @Cacheable(value = "customerCache", key = "'feedback-'+#feedback", unless = "#result == null || #result.isEmpty()")
    public List<Customer> findCustomerByFeedback(String feedback) {
        logger.info("Finding customers by feedback: {}", feedback);
        if (feedback == null || feedback.trim().isEmpty()) {
            throw new IllegalArgumentException("Feedback cannot be null or empty");
        }
        List<Customer> customers = c.findByFeedbackContainingIgnoreCase(feedback.trim());
        customers.removeIf(Customer::isDeleted);
        return customers;
    }

    // Find customers by ratings
    @Cacheable(value = "customerCache", key = "'ratings-'+#rating", unless = "#result == null || #result.isEmpty()")
    public List<Customer> findCustomersByRatings(int rating) {
        logger.info("Finding customers by rating: {}", rating);
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        List<Customer> customers = c.findByRatingGreaterThanEqual(rating);
        customers.removeIf(Customer::isDeleted);
        return customers;
    }
}
