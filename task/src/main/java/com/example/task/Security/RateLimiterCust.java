package com.example.task.Security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimiterCust{
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    protected Bucket createNewBucket() {
        // Allow 5 requests per minute
        Bandwidth limit = Bandwidth.classic(2, Refill.intervally(2, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    public boolean tryConsume(String apiKey) {
        Bucket bucket = cache.computeIfAbsent(apiKey, k -> createNewBucket());
        return bucket.tryConsume(1);
    }

}
