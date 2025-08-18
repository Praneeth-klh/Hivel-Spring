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



    @Cacheable(value = "customerCache", key = "#page + '-' + #size + '-' + #sortBy + '-' + #order")
    public Page<Customer> findCustomers(int page, int size, String sortBy, String order) {
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "id";
        }
        if (order == null || order.isEmpty()) {
            order = "asc";
        }
        Sort.Direction direction = order.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return c.findAllByIsDeletedFalse(pageable);
    }

    public List<Customer> searchCustomersByName(String name) {
        List<Customer> allCustomers = (List<Customer>) cacheManager.getCache("customerCache").get("allCustomers").get();
        return allCustomers.stream()
                .filter(customer -> customer.getFirstName().toLowerCase().contains(name.toLowerCase()) ||
                        customer.getLastName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "customerCache", key = "#id")
    public Customer updateCustomerDetails(Long id, String firstName, String lastName) {
        Optional<Customer> customerOpt = c.findById(id);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            if (firstName != null && !firstName.isEmpty()) {
                customer.setFirstName(firstName);
            }
            if (lastName != null && !lastName.isEmpty()) {
                customer.setLastName(lastName);
            }
            return c.save(customer);
        }
        return null;
    }

    public Customer updateCustomerFeedback(String feedback, String firstName) {
        List<Customer> customers = c.findByFirstNameIgnoreCase(firstName);
        if (!customers.isEmpty()) {
            Customer customer = customers.get(0);
            if (feedback != null && !feedback.isEmpty()) {
                customer.setFeedback(feedback);
            }
            return c.save(customer);
        }
        return null;
    }

    public boolean deleteCustomerByIdHard(Long id) {
        Optional<Customer> optionalCustomer = c.findById(id);
        if (optionalCustomer.isPresent() && !optionalCustomer.get().isDeleted()) {
            c.deleteById(id);
            return true;
        }
        return false;
    }

    @CacheEvict(value = "customerCache", key = "#id")
    public boolean deleteCustomerByIdSoft(Long id) {
        Optional<Customer> optionalCustomer = c.findById(id);
        if (optionalCustomer.isPresent() && !optionalCustomer.get().isDeleted()) {
            optionalCustomer.get().setDeleted(true);
            c.save(optionalCustomer.get());
            return true;
        }
        return false;
    }

    public void saveCustomer(Customer customer) {
        c.save(customer);
    }

    public void saveCustomers(List<Customer> customers) {
        LocalDateTime currentTime = LocalDateTime.now();
        for (Customer customer : customers) {
            if (customer.getId() == null) {
                customer.setCreatedAt(currentTime);
                customer.setCreatedBy(customer.getFirstName());
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

    private void logCustomerAudit(Customer customer, String action) {
        CustomerAudits audit = new CustomerAudits(customer, action, LocalDateTime.now());
        customerAuditRepository.save(audit);
    }
    @Cacheable(value = "customerCache", key = "'firstName-'+#s", unless = "#result == null || #result.isEmpty()")
    public List<Customer> findCustomerByFirstName(String s) {
        if (s == null || s.trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be null or empty");
        }
        List<Customer> customers = c.findByFirstNameIgnoreCase(s.trim());
        customers.removeIf(Customer::isDeleted);
        return customers;
    }

    @Cacheable(value = "customerCache", key = "'lastName-'+#lastName", unless = "#result == null || #result.isEmpty()")
    public List<Customer> findCustomerByLastName(String lastName) {
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be null or empty");
        }
        List<Customer> customers = c.findByLastNameIgnoreCase(lastName.trim());
        customers.removeIf(Customer::isDeleted);
        return customers;
    }

    @Cacheable(value = "customerCache", key = "'feedback-'+#feedback", unless = "#result == null || #result.isEmpty()")
    public List<Customer> findCustomerByFeedback(String feedback) {
        if (feedback == null || feedback.trim().isEmpty()) {
            throw new IllegalArgumentException("Feedback cannot be null or empty");
        }
        List<Customer> customers = c.findByFeedbackContainingIgnoreCase(feedback.trim());
        customers.removeIf(Customer::isDeleted);
        return customers;
    }

    @Cacheable(value = "customerCache", key = "'ratings-'+#rating", unless = "#result == null || #result.isEmpty()")
    public List<Customer> findCustomersByRatings(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        List<Customer> customers = c.findByRatingGreaterThanEqual(rating);
        customers.removeIf(Customer::isDeleted);
        return customers;
    }



}
