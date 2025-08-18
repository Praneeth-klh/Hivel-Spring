package com.example.task.controller;

import com.example.task.Service.MgmtSer;
import com.example.task.Security.JwtUtil;
import com.example.task.model.Mgmt;
import com.example.task.dto.LoginRequest;
import com.example.task.dto.LoginResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/Mgmt")
@CrossOrigin(origins = "http://localhost:3000")
public class ControllerMgmt {

    private final MgmtSer mgmtSer;
    private final JwtUtil jwtUtil;

    public ControllerMgmt(MgmtSer mgmtSer, JwtUtil jwtUtil) {
        this.mgmtSer = mgmtSer;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Mgmt mgmt = mgmtSer.findByUsernameAndPassword(request.getUsername(), request.getPassword());

        if (mgmt != null) {
            String token = jwtUtil.generateToken(mgmt.getUsername());
            return ResponseEntity.ok(new LoginResponse(token));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }
}
