package com.example.task.repository;

import com.example.task.model.mgmtaudits;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Audits extends JpaRepository<mgmtaudits, Long> {
}
