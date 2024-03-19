package ru.itfb.com.ratelimiter.controller;


import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/ratelimiter/resource")
public class RateLimiterResourceController {

    private static final Logger LOGGER = Logger.getLogger(RateLimiterResourceController.class.getSimpleName());

    @GetMapping("/first")
    @RateLimiter(name = "rateLimitingAPI")
    public ResponseEntity<String> getResourceFirst() throws InterruptedException {
        LOGGER.log(Level.INFO, "Get first rate limiter resource operation is performed");
        Thread.sleep(3000);
        return ResponseEntity.ok("Get first rate limiter resource operation is performed");
    }

    @GetMapping("/second")
    public ResponseEntity<String> getResourceSecond() throws InterruptedException {
        LOGGER.log(Level.INFO, "Get second rate limiter resource operation is performed");
        Thread.sleep(3000);
        return ResponseEntity.ok("Get second rate limiter resource operation is performed");
    }
}
