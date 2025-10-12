package com.innowise.internship.exception;

public class UserNotFoundException extends RuntimeException {

  public UserNotFoundException(Long userId) {
    super("User with ID " + userId + " not found in User Service.");
  }

  public UserNotFoundException(String email) {
    super("User with email '" + email + "' not found in User Service.");
  }

}
