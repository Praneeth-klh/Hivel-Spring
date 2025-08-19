package com.example.task.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.task.model.Customer;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface Customerepo extends JpaRepository<Customer, Long> {

    @Query("SELECT c FROM Customer c WHERE c.isDeleted = false AND LOWER(c.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))")
    Page<Customer> findByFirstNameIgnoreCaseContainingAndIsDeletedFalse(String firstName, Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE c.isDeleted = false AND LOWER(c.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))")
    Page<Customer> findByLastNameIgnoreCaseContainingAndIsDeletedFalse(String lastName, Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE c.isDeleted = false AND c.rating >= :rating")
    Page<Customer> findByRatingGreaterThanEqualAndIsDeletedFalse(int rating, Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE c.isDeleted = false AND LOWER(c.feedback) LIKE LOWER(CONCAT('%', :feedback, '%'))")
    Page<Customer> findByFeedbackContainingIgnoreCaseAndIsDeletedFalse(String feedback, Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE c.isDeleted = false")
    Page<Customer> findAllByIsDeletedFalse(Pageable pageable);

    List<Customer> findByFirstNameIgnoreCase(String firstName);
    List<Customer> findByLastNameIgnoreCase(String lastName);
    List<Customer> findByFeedbackContainingIgnoreCase(String feedback);
    List<Customer> findByRatingGreaterThanEqual(int rating);
    List<Customer> findByIsDeletedTrue();
    List<Customer> findById(int id);
}
