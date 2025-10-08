package com.innowise.internship.exception;

public class UserServiceResponseException extends RuntimeException {

  public UserServiceResponseException(String message, Throwable cause) {
    super(message, cause);
  }

  public UserServiceResponseException(String message) {
    super(message);
  }
}
