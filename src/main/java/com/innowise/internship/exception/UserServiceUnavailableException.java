package com.innowise.internship.exception;

public class UserServiceUnavailableException extends RuntimeException {

  public UserServiceUnavailableException(String message, Throwable cause) {
    super("User Service communication failed: " + message, cause);
  }

  public UserServiceUnavailableException(String message) {
    super("User Service communication failed: " + message);
  }
}
