package com.innowise.internship.entity;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

  private Long id;
  private Long userId;
  private OrderStatus status;
  private Instant creationDate;
  private List<OrderItem> items;

}
