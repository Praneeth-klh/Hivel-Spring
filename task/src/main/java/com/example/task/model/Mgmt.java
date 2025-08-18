package com.example.task.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.antlr.v4.runtime.misc.NotNull;

@Entity
public class Mgmt {
    @Id  // This field will be the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Automatically generate ID value
    private Long id;  // Primary key
    @NotNull
    private String username;
    @NotNull
    private String password;
    public Mgmt(){}
    public Mgmt(String username, String password){
        this.username = username;
        this.password = password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getUsername() {
        return username;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getId() {
        return id;
    }
}
