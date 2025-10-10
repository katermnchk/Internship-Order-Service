package com.innowise.internship.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResponse {
  @JsonProperty("isValid")
  private boolean isValid;
  private String message;
  private String userId;
}
