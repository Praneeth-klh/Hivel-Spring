package com.example.task.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
@Entity // Add this annotation
@Table(name = "customer_audits")
public class CustomerAudits {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer; // The customer being acted upon

    private String action; // Action: created, updated, deleted, etc.

    private LocalDateTime timestamp; // Timestamp of the action

    private String performedBy; // User who performed the action

    // Constructors, getters, and setters
    public CustomerAudits() {}

    public CustomerAudits(Customer customer, String action, LocalDateTime timestamp) {
        this.customer = customer;
        this.action = action;
        this.timestamp = timestamp;
        this.performedBy = performedBy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

}
