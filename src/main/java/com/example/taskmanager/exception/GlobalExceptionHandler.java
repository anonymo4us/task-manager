package com.example.taskmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;



@RestControllerAdvice
public class GlobalExceptionHandler {

  public record ApiError(String error, String message) {}

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException exception) {
    ApiError body = new ApiError("Not Found",
            exception.getMessage());

    return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(body);
  }
}
