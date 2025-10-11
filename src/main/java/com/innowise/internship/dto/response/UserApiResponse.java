package com.innowise.internship.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.innowise.internship.dto.UserDTO;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class UserApiResponse {

  private UserDTO data;

}