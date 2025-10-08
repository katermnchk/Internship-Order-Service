package com.innowise.internship.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequestDTO {

  @NotNull(message = "User ID must not be null")
  Long userId;

  @NotBlank(message = "Status cannot be empty")
  String status;

  @Valid
  @Size(min = 1, message = "Order must contain at least one item")
  @NotNull(message = "Order items list cannot be null")
  List<OrderItemDTO> items;

}
