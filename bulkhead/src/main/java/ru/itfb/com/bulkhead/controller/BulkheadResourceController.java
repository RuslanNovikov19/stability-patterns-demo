package ru.itfb.com.bulkhead.controller;


import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/bulkhead/resource")
public class BulkheadResourceController {

    private static final Logger LOGGER = Logger.getLogger(BulkheadResourceController.class.getSimpleName());

    @GetMapping("/first")
    @Bulkhead(name = "FIRST_BULKHEAD")
    public ResponseEntity<String> getFirstResource() throws InterruptedException {
        LOGGER.log(Level.INFO, "Get first resource operation is performed");
        Thread.sleep(5000);
        return ResponseEntity.ok("Get first resource operation is performed");
    }

    @GetMapping("/second")
    @Bulkhead(name = "SECOND_BULKHEAD")
    public ResponseEntity<String> getSecondResource() throws InterruptedException {
        LOGGER.log(Level.INFO, "Get second resource operation is performed");
        Thread.sleep(5000);
        return ResponseEntity.ok("Get second resource operation is performed");
    }

    @GetMapping("/third")
    public ResponseEntity<String> getThirdResource() throws InterruptedException {
        LOGGER.log(Level.INFO, "Get third resource operation is performed");
        Thread.sleep(8000);
        return ResponseEntity.ok("Get third resource operation is performed");
    }
}
