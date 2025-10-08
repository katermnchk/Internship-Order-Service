package com.innowise.internship.controller;

import com.innowise.internship.dto.response.ApiErrorResponse;
import com.innowise.internship.exception.ErrorMessages;
import com.innowise.internship.exception.ItemNotFoundException;
import com.innowise.internship.exception.OrderNotFoundException;
import com.innowise.internship.exception.UserNotFoundException;
import com.innowise.internship.exception.UserServiceUnavailableException;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler({
      OrderNotFoundException.class,
      UserNotFoundException.class,
      ItemNotFoundException.class
  })
  public ResponseEntity<ApiErrorResponse> handleNotFoundException(RuntimeException e) {
    log.warn("Entity not found: {}", e.getMessage(), e);
    return buildErrorResponse(
        HttpStatus.NOT_FOUND,
        List.of(e.getMessage()),
        ErrorMessages.NOT_FOUND
    );
  }

  @ExceptionHandler({ UserServiceUnavailableException.class})
  public ResponseEntity<ApiErrorResponse> handleServiceUnavailable(RuntimeException e) {
    log.error("Service unavailable failed: {}", e.getMessage(), e);
    return buildErrorResponse(
        HttpStatus.SERVICE_UNAVAILABLE,
        List.of(e.getMessage()),
        ErrorMessages.SERVICE_UNAVAILABLE
    );
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
    List<String> errors = e.getBindingResult().getFieldErrors().stream()
        .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
        .toList();
    log.info("Validation failed: {} errors", errors.size());
    errors.forEach(error -> log.debug("Validation error: {}", error));
    return buildErrorResponse(
        HttpStatus.BAD_REQUEST,
        errors,
        ErrorMessages.BAD_REQUEST);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException e) {
    List<String> errors = e.getConstraintViolations().stream()
        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
        .toList();
    log.info("Constraint violations: {} errors", errors.size());
    errors.forEach(error -> log.debug("Constraint violation: {}", error));
    return buildErrorResponse(
        HttpStatus.BAD_REQUEST,
        errors,
        ErrorMessages.BAD_REQUEST);
  }

  @ExceptionHandler({
      HttpMessageNotReadableException.class,
      MethodArgumentTypeMismatchException.class,
      IllegalArgumentException.class })
  public ResponseEntity<ApiErrorResponse> handleClientBadRequests(RuntimeException e) {
    String message = "Invalid request format or parameter type.";

    if (e instanceof HttpMessageNotReadableException) {
      message = "Malformed JSON request body.";
    } else if (e instanceof IllegalArgumentException) {
      message = "Invalid status value provided (ENUM mismatch).";
    }

    log.info("Client bad request: {}", e.getMessage());
    return buildErrorResponse(
        HttpStatus.BAD_REQUEST,
        List.of(message),
        ErrorMessages.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleAllExceptions(Exception e) {
    log.error("Unexpected error: {}", e.getMessage(), e);
    return buildErrorResponse(
        HttpStatus.INTERNAL_SERVER_ERROR,
        List.of("Unexpected error occurred on server"),
        ErrorMessages.INTERNAL_SERVER_ERROR
    );
  }

  private ResponseEntity<ApiErrorResponse> buildErrorResponse(
      HttpStatus status,
      List<String> messages,
      String error
  ) {
    ApiErrorResponse response = new ApiErrorResponse(status.value(), messages, error);
    return new ResponseEntity<>(response, status);
  }

}
