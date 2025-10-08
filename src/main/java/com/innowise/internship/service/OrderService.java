package com.innowise.internship.service;

import com.innowise.internship.dto.OrderCreateRequestDTO;
import com.innowise.internship.dto.OrderResponseDTO;
import com.innowise.internship.entity.OrderStatus;
import java.util.List;

public interface OrderService {

  OrderResponseDTO createOrder(OrderCreateRequestDTO dto);
  OrderResponseDTO getOrderById(Long id);
  List<OrderResponseDTO> getOrdersByIds(List<Long> ids);
  List<OrderResponseDTO> getOrdersByStatuses(List<OrderStatus> statuses);
  OrderResponseDTO updateOrder(Long id, OrderCreateRequestDTO dto);
  void deleteOrderById(Long id);

}
