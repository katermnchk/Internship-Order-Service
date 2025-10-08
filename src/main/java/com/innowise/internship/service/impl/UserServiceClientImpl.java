package com.innowise.internship.service.impl;

import com.innowise.internship.dto.UserDTO;
import com.innowise.internship.exception.UserNotFoundException;
import com.innowise.internship.exception.UserServiceUnavailableException;
import com.innowise.internship.service.UserServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class UserServiceClientImpl implements UserServiceClient {

  private final RestTemplate restTemplate;

  @Value("${user.service.url}")
  private String userServiceBaseUrl;

  @Override
  public UserDTO getUserById(Long userId) {
    String url = userServiceBaseUrl + "/users/" + userId;

    try {
      ResponseEntity<UserDTO> response = restTemplate.getForEntity(url, UserDTO.class);
      return response.getBody();
    } catch (HttpClientErrorException.NotFound e) {
      throw new UserNotFoundException(userId);
    } catch (RestClientException e) {
      throw new UserServiceUnavailableException(e.getMessage(), e);
    }
  }

  @Override
  public UserDTO getUserByEmail(String email) {
    String url = userServiceBaseUrl + "/users/by-email?email={email}";

    try {
      ResponseEntity<UserDTO> response = restTemplate.getForEntity(url, UserDTO.class, email);
      return response.getBody();
    } catch (HttpClientErrorException.NotFound e) {
      throw new UserNotFoundException(email);
    } catch (RestClientException e) {
      throw new UserServiceUnavailableException(e.getMessage(), e);
    }

  }
}
