package remitly.task.stockmarket.controller;

import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import remitly.task.stockmarket.exceptions.InsufficientStockException;
import remitly.task.stockmarket.exceptions.StockNotFoundException;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIllegalArgument(IllegalArgumentException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(InsufficientStockException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleInsufficientStock(InsufficientStockException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(StockNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleStockNotFound(StockNotFoundException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler({OptimisticLockException.class, ObjectOptimisticLockingFailureException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleOptimisticLock(Exception ex) {
        log.warn("Optimistic lock conflict — concurrent modification detected: {}", ex.getMessage());
        return Map.of("error", "Concurrent modification detected, please retry");
    }

    @ExceptionHandler({LockTimeoutException.class, QueryTimeoutException.class})
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, String> handleLockTimeout(Exception ex) {
        log.warn("Lock timeout — system under heavy load: {}", ex.getMessage());
        return Map.of("error", "System is under heavy load, please retry in a moment");
    }
}