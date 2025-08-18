package com.example.task.repository;

import com.example.task.model.Customer;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface Paging extends PagingAndSortingRepository<Customer, Long> {
}
