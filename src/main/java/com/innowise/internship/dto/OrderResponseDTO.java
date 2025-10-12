package com.innowise.internship.dto;

import com.innowise.internship.entity.OrderStatus;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {

  Long id;
  Long userId;
  OrderStatus status;
  Instant creationDate;

  UserDTO userInfo;
  List<OrderItemDTO> items;

}
