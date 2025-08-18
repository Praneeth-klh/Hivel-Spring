package com.example.task.Security;

import com.example.task.Service.MgmtSer;
import com.example.task.model.Mgmt;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final MgmtSer mgmtSer;

    public JwtAuthFilter(JwtUtil jwtUtil, MgmtSer mgmtSer) {
        this.jwtUtil = jwtUtil;
        this.mgmtSer = mgmtSer;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String header = req.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            // Validate JWT
            if (jwtUtil.validateToken(token)) {
                // Extract username from JWT
                String username = jwtUtil.getUsername(token);

                // Load Mgmt entity from your service (no password needed here)
                Mgmt mgmt = mgmtSer.findByUsernameAndPassword(username, null); // password is null since we already validated JWT

                if (mgmt != null) {
                    // Create Spring Security authentication
                    var auth = new UsernamePasswordAuthenticationToken(
                            mgmt.getUsername(),
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }

        chain.doFilter(req, res);
    }
}
