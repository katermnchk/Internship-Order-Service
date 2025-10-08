package com.innowise.internship.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import lombok.Value;


@Value
public class UserDTO {

  @JsonProperty("userId")
  Long id;

  @JsonProperty("userName")
  String name;

  @JsonProperty("userSurname")
  String surname;

  @JsonProperty("userBirthDate")
  LocalDate birthDate;

  @JsonProperty("userEmail")
  String email;

}