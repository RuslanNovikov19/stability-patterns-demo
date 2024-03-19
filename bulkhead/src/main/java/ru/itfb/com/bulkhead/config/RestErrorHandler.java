package ru.itfb.com.bulkhead.config;

import io.github.resilience4j.bulkhead.BulkheadFullException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Log4j2
public class RestErrorHandler {

    @ExceptionHandler(BulkheadFullException.class)
    public ResponseEntity<String> bulkheadFullException(BulkheadFullException exception) {
        log.error("Bulkhead full exception caused by call number");
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(exception.getMessage());
    }
}
