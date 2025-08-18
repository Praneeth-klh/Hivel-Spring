package com.example.task.repository;

import com.example.task.model.CustomerAudits;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerA extends JpaRepository<CustomerAudits, Long> {
}
