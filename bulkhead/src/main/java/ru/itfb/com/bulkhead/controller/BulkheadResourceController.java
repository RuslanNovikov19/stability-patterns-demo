package ru.itfb.com.bulkhead.controller;


import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/bulkhead/resource")
public class BulkheadResourceController {

    private static final Logger LOGGER = Logger.getLogger(BulkheadResourceController.class.getSimpleName());

    @GetMapping
    @Bulkhead(name = "RESOURCE_BULKHEAD")
    public void getResource() throws InterruptedException {
        LOGGER.log(Level.INFO, "Get resource operation is performed");
        Thread.sleep(1000);
    }
}
