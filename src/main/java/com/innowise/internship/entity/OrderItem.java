package com.innowise.internship.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

  private Long id;
  private Long orderId;
  private Long itemId;
  private int quantity;

}
