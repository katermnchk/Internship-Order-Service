package com.innowise.internship.dto.response;

import java.time.OffsetDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthServiceApiResponse<T> {
  private int status;
  private String message;
  private T data;
  private OffsetDateTime timestamp;
}
