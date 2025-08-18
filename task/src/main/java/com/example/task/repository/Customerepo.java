package com.example.task.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.task.model.Customer;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface Customerepo extends JpaRepository<Customer, Long> {
    @Query("SELECT c FROM Customer c WHERE c.isDeleted = false")
    Page<Customer> findAllByIsDeletedFalse(Pageable pageable);
    List<Customer> findByFirstNameIgnoreCase(String firstName);
    List<Customer> findByLastNameIgnoreCase(String lastName);
    List<Customer> findByFeedbackContainingIgnoreCase(String feedback);
    List<Customer> findByRatingGreaterThanEqual(int rating);
}
