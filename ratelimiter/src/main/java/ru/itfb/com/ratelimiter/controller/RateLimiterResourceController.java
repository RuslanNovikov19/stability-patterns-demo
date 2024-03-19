package ru.itfb.com.ratelimiter.controller;


import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/ratelimiter/resource")
public class RateLimiterResourceController {

    private static final Logger LOGGER = Logger.getLogger(RateLimiterResourceController.class.getSimpleName());

    @GetMapping
    @RateLimiter(name = "rateLimitingAPI")
    public void getResource() throws InterruptedException {
        LOGGER.log(Level.INFO, "Get resource operation is performed");
        Thread.sleep(1000);
    }
}
