package com.innowise.internship.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.internship.dto.response.AuthServiceApiResponse;
import com.innowise.internship.dto.response.ValidationResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthServiceTokenFilter extends OncePerRequestFilter {

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  @Value("${app.auth-service.validate-url}")
  private String validateUrl;

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {

    final String authHeader = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    final String token = authHeader.substring(7);

    try {
      String urlWithToken = UriComponentsBuilder.fromHttpUrl(validateUrl)
          .queryParam("accessToken", token)
          .toUriString();

      ResponseEntity<String> responseEntity = restTemplate.getForEntity(urlWithToken, String.class);

      AuthServiceApiResponse<ValidationResponse> apiResponse = objectMapper.readValue(
          responseEntity.getBody(),
          new TypeReference<>() {}
      );

      ValidationResponse validationData = apiResponse.getData();

      if (validationData != null && validationData.isValid()) {
        String userId = validationData.getUserId();
        var authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(userId, token, authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("Authenticated user with ID: {}", userId);
      } else {
        SecurityContextHolder.clearContext();
      }

    } catch (RestClientException e) {
      log.error("Token validation request failed. Reason: {}", e.getMessage());
      SecurityContextHolder.clearContext();
    } catch (IOException e) {
      log.error("Failed to parse validation response. Reason: {}", e.getMessage());
      SecurityContextHolder.clearContext();
    }

    filterChain.doFilter(request, response);
  }
}
