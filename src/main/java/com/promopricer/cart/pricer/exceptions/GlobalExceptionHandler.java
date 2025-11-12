package com.promopricer.cart.pricer.exceptions;

import com.promopricer.cart.pricer.controller.domains.ErrorResponse;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return new ResponseEntity<>(new ErrorResponse(404, "Not Found", ex.getMessage(), "/cart"), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
        return new ResponseEntity<>(new ErrorResponse(409, "Conflict", ex.getMessage(), "/cart/confirm"), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLocking(OptimisticLockingFailureException ex) {
        return new ResponseEntity<>(new ErrorResponse(409, "Conflict", "Concurrent update failed. Please retry the transaction.", "/cart/confirm"), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String errorMsg = ex.getBindingResult().getFieldError().getDefaultMessage();
        return new ResponseEntity<>(new ErrorResponse(400, "Bad Request", "Validation failed: " + errorMsg, "/"), HttpStatus.BAD_REQUEST);
    }
}
