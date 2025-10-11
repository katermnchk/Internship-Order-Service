package com.innowise.internship.service.impl;

import com.innowise.internship.dto.UserDTO;
import com.innowise.internship.dto.response.UserApiResponse;
import com.innowise.internship.exception.UserNotFoundException;
import com.innowise.internship.exception.UserServiceUnavailableException;
import com.innowise.internship.service.UserServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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
      String token = (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();

      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(token);
      HttpEntity<Void> entity = new HttpEntity<>(headers);

      ResponseEntity<UserApiResponse> response = restTemplate.exchange(
          url,
          HttpMethod.GET,
          entity,
          UserApiResponse.class
      );

      UserApiResponse apiResponse = response.getBody();
      if (apiResponse != null) {
        return apiResponse.getData();
      }
      return null;

    } catch (HttpClientErrorException.Forbidden e) {
      throw new UserServiceUnavailableException("Access denied to User Service", e);
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
      String token = (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();
      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(token);
      HttpEntity<Void> entity = new HttpEntity<>(headers);

      ResponseEntity<UserDTO> response = restTemplate.exchange(
          url,
          HttpMethod.GET,
          entity,
          UserDTO.class,
          email
      );

      return response.getBody();

    } catch (HttpClientErrorException.Forbidden e) {
      throw new UserServiceUnavailableException("Access denied to User Service", e);
    } catch (HttpClientErrorException.NotFound e) {
      throw new UserNotFoundException(email);
    } catch (RestClientException e) {
      throw new UserServiceUnavailableException(e.getMessage(), e);
    }
  }
}

