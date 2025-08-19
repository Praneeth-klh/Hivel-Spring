package com.example.task.repository;

import com.example.task.model.Mgmt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Mgmtrepo extends JpaRepository<Mgmt, String> {
    Optional<Mgmt> findByUsername(String username);
}
