package com.innowise.internship.service;

import com.innowise.internship.dto.OrderCreateRequestDTO;
import com.innowise.internship.dto.OrderResponseDTO;
import com.innowise.internship.entity.OrderStatus;
import java.util.List;
import java.util.Optional;

public interface OrderService {

  OrderResponseDTO createOrder(OrderCreateRequestDTO dto);
  Optional<OrderResponseDTO> getOrderById(Long id);
  List<OrderResponseDTO> getOrdersByIds(List<Long> ids);
  List<OrderResponseDTO> getOrdersByStatuses(List<OrderStatus> statuses);
  OrderResponseDTO updateOrder(Long id, OrderCreateRequestDTO dto);
  void deleteOrderById(Long id);

}
