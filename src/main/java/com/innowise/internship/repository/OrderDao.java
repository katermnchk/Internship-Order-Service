package com.innowise.internship.repository;

import com.innowise.internship.entity.Order;
import com.innowise.internship.entity.OrderStatus;
import java.util.List;
import java.util.Optional;

public interface OrderDao {

  Order save(Order order);
  Optional<Order> findById(Long id);
  List<Order> findByIds(List<Long> ids);
  List<Order> findByStatuses(List<OrderStatus> statuses);
  int update(Order order);
  int delete(Long id);

}
