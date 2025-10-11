package com.innowise.internship.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class AuthServiceClient {

  private final RestTemplate restTemplate;

  @Value("${app.auth-service.login-url}")
  private String loginUrl;

  public AuthServiceClient(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public String getToken(String username, String password) {
    Map<String, String> body = Map.of("username", username, "password", password);
    Map<String, Object> response = restTemplate.postForObject(loginUrl, body, Map.class);
    return (String) response.get("token");
  }
}
