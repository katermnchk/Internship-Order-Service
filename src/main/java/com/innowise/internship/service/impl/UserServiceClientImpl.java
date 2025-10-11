package com.innowise.internship.service.impl;

import com.innowise.internship.dto.UserDTO;
import com.innowise.internship.exception.UserNotFoundException;
import com.innowise.internship.exception.UserServiceUnavailableException;
import com.innowise.internship.service.UserServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class UserServiceClientImpl implements UserServiceClient {

  private final RestTemplate restTemplate;
  private final AuthServiceClient authServiceClient;

  @Value("${user.service.url}")
  private String userServiceBaseUrl;

  private HttpHeaders createHeadersWithToken() {
    HttpHeaders headers = new HttpHeaders();
    String token = authServiceClient.getToken("order-service", "secret");
    headers.setBearerAuth(token);
    return headers;
  }

  @Override
  public UserDTO getUserById(Long userId) {
    String url = userServiceBaseUrl + "/users/" + userId;

    try {
      HttpEntity<Void> entity = new HttpEntity<>(createHeadersWithToken());

      ResponseEntity<UserDTO> response = restTemplate.exchange(
          url,
          HttpMethod.GET,
          entity,
          UserDTO.class
      );

      return response.getBody();
    } catch (HttpClientErrorException.NotFound e) {
      throw new UserNotFoundException(userId);
    } catch (RestClientException e) {
      throw new UserServiceUnavailableException(e.getMessage(), e);
    }
  }

  @Override
  public UserDTO getUserByEmail(String email) {
    String url = userServiceBaseUrl + "/users/by-email?email=" + email;

    try {
      HttpEntity<Void> entity = new HttpEntity<>(createHeadersWithToken());

      ResponseEntity<UserDTO> response = restTemplate.exchange(
          url,
          HttpMethod.GET,
          entity,
          UserDTO.class
      );

      return response.getBody();
    } catch (HttpClientErrorException.NotFound e) {
      throw new UserNotFoundException(email);
    } catch (RestClientException e) {
      throw new UserServiceUnavailableException(e.getMessage(), e);
    }
  }
}
