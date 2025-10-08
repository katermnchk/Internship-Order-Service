package com.innowise.internship.service.impl;

import com.innowise.internship.dto.OrderCreateRequestDTO;
import com.innowise.internship.dto.OrderResponseDTO;
import com.innowise.internship.dto.UserDTO;
import com.innowise.internship.entity.Order;
import com.innowise.internship.entity.OrderItem;
import com.innowise.internship.entity.OrderStatus;
import com.innowise.internship.exception.OrderNotFoundException;
import com.innowise.internship.exception.UserNotFoundException;
import com.innowise.internship.exception.UserServiceUnavailableException;
import com.innowise.internship.mapper.OrderItemMapper;
import com.innowise.internship.mapper.OrderMapper;
import com.innowise.internship.repository.OrderDao;
import com.innowise.internship.repository.OrderItemDao;
import com.innowise.internship.service.OrderService;
import com.innowise.internship.service.UserServiceClient;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

  private final OrderDao orderDao;
  private final OrderItemDao orderItemDao;
  private final OrderMapper orderMapper;
  private final OrderItemMapper orderItemMapper;
  private final UserServiceClient userClient;

  private void loadOrderItems(Order order) {
    if (order.getItems() == null) {
      order.setItems(orderItemDao.findByOrderId(order.getUserId()));
    }
  }

  private OrderResponseDTO enrichResponse(Order order) {
    loadOrderItems(order);
    UserDTO userInfo = userClient.getUserById(order.getId());
    OrderResponseDTO baseResponse = orderMapper.toResponseDto(order);

    return new OrderResponseDTO(
        baseResponse.getId(),
        baseResponse.getUserId(),
        baseResponse.getStatus(),
        baseResponse.getCreationDate(),
        userInfo,
        baseResponse.getItems()
    );
  }

  @Transactional
  @Override
  public OrderResponseDTO createOrder(OrderCreateRequestDTO orderDto) {
    Order order = orderMapper.toEntity(orderDto);
    order.setStatus(OrderStatus.valueOf(orderDto.getStatus().toUpperCase()));

    Order savedOrder = orderDao.save(order);

    List<OrderItem> items = orderItemMapper.toEntityList(orderDto.getItems());
    items.forEach(item -> item.setOrderId(savedOrder.getId()));
    orderItemDao.saveAll(items);
    savedOrder.setItems(items);

    return enrichResponse(savedOrder);
  }

  @Override
  public Optional<OrderResponseDTO> getOrderById(Long id) {
    return orderDao.findById(id)
        .map(this::enrichResponse);
  }

  @Override
  public List<OrderResponseDTO> getOrdersByIds(List<Long> ids) {
    return orderDao.findByIds(ids).stream()
        .map(this::enrichResponse)
        .collect(Collectors.toList());
  }

  @Override
  public List<OrderResponseDTO> getOrdersByStatuses(List<OrderStatus> statuses) {
    return orderDao.findByStatuses(statuses).stream()
        .map(this::enrichResponse)
        .collect(Collectors.toList());
  }

  @Transactional
  @Override
  public OrderResponseDTO updateOrder(Long id, OrderCreateRequestDTO dto) {
    Order existingOrder = orderDao.findById(id)
        .orElseThrow(() -> new OrderNotFoundException(id));

    existingOrder.setUserId(dto.getUserId());
    existingOrder.setStatus(OrderStatus.valueOf(dto.getStatus().toUpperCase()));

    orderDao.update(existingOrder);

    orderItemDao.deleteByOrderId(id);
    List<OrderItem> newItems = orderItemMapper.toEntityList(dto.getItems());
    newItems.forEach(item -> item.setOrderId(id));
    orderItemDao.saveAll(newItems);
    existingOrder.setItems(newItems);

    return enrichResponse(existingOrder);
  }

  @Transactional
  @Override
  public void deleteOrderById(Long id) {
    if (orderDao.findById(id).isEmpty()) {
      throw new OrderNotFoundException(id);
    }

    orderItemDao.deleteByOrderId(id);

    orderDao.delete(id);
  }
}
