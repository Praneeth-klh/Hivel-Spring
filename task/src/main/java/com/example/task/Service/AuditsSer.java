package com.example.task.Service;

import com.example.task.model.Customer;
import com.example.task.model.Mgmt;
import com.example.task.model.mgmtaudits;
import com.example.task.repository.Audits;
import com.example.task.repository.Customerepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuditsSer {

    @Autowired
    private Customerepo customerRepository;

    @Autowired
    private Audits managementAuditRepository;
    private Mgmt getCurrentAdmin() {

        return new Mgmt("Admin", "password");
    }

    // Create feedback and log it to the audit table
    public Customer createCustomerFeedback(Customer customer) {
        Customer savedCustomer = customerRepository.save(customer);
        logAudit(savedCustomer.getId(), "created");
        return savedCustomer;
    }

    // Update feedback and log it to the audit table
    public Customer updateCustomerFeedback(Long id, Customer customer) {
        Optional<Customer> existingCustomer = customerRepository.findById(id);
        if (existingCustomer.isPresent()) {
            Customer updatedCustomer = existingCustomer.get();
            updatedCustomer.setFeedback(customer.getFeedback());
            updatedCustomer.setRating(customer.getRating());
            Customer savedCustomer = customerRepository.save(updatedCustomer);
            logAudit(savedCustomer.getId(), "updated");  // Log the audit for update action
            return savedCustomer;
        }
        return null;  // Or throw an exception if customer not found
    }

    // Soft delete feedback and log it to the audit table
    public void softDeleteFeedback(Long id) {
        Optional<Customer> existingCustomer = customerRepository.findById(id);
        if (existingCustomer.isPresent()) {
            Customer deletedCustomer = existingCustomer.get();
            deletedCustomer.setDeleted(true);  // Mark as deleted
            customerRepository.save(deletedCustomer);  // Save the updated customer object
            logAudit(deletedCustomer.getId(), "deleted");  // Log the audit for soft deletion
        }
    }

    // Log the audit action to the ManagementAudit table (mgmtaudits table)
    private void logAudit(Long feedbackId, String action) {
        Mgmt admin = getCurrentAdmin();  // Get the current admin performing the action
        mgmtaudits audit = new mgmtaudits(admin, action, feedbackId, LocalDateTime.now());  // Create a new audit record
        managementAuditRepository.save(audit);  // Save the audit log to the database
    }
}
