package com.innowise.internship.repository;

import com.innowise.internship.entity.OrderItem;
import java.util.List;

public interface OrderItemDao {

  void saveAll(List<OrderItem> items);
  List<OrderItem> findByOrderId(Long orderId);
  int deleteByOrderId(Long orderId);

}
