package com.innowise.internship.service;

import com.innowise.internship.dto.UserDTO;

public interface UserServiceClient {

  UserDTO getUserById(Long userId);
  UserDTO getUserByEmail(String email);

}
