package com.innowise.internship.service.impl;

import com.innowise.internship.dto.OrderCreateRequestDTO;
import com.innowise.internship.dto.OrderResponseDTO;
import com.innowise.internship.dto.UserDTO;
import com.innowise.internship.dto.kafka.OrderCreatedEvent;
import com.innowise.internship.entity.Item;
import com.innowise.internship.entity.Order;
import com.innowise.internship.entity.OrderItem;
import com.innowise.internship.entity.OrderStatus;
import com.innowise.internship.exception.OrderNotFoundException;
import com.innowise.internship.mapper.OrderItemMapper;
import com.innowise.internship.mapper.OrderMapper;
import com.innowise.internship.repository.ItemDao;
import com.innowise.internship.repository.OrderDao;
import com.innowise.internship.repository.OrderItemDao;
import com.innowise.internship.service.KafkaProducerService;
import com.innowise.internship.service.OrderService;
import com.innowise.internship.service.UserServiceClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

  private final OrderDao orderDao;
  private final OrderItemDao orderItemDao;
  private final OrderMapper orderMapper;
  private final OrderItemMapper orderItemMapper;
  private final UserServiceClient userClient;
  private final ItemDao itemDao;
  private final KafkaProducerService kafkaProducerService;

  private void loadOrderItems(Order order) {
    if (order.getItems() == null) {
      order.setItems(orderItemDao.findByOrderId(order.getId()));
    }
  }

  private OrderResponseDTO enrichResponse(Order order) {
    loadOrderItems(order);
    UserDTO userInfo = userClient.getUserById(order.getUserId());
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

    Order initialOrder = orderDao.save(order);
    Order savedOrder = orderDao.findById(initialOrder.getId()).get();

    List<OrderItem> itemsToSave = orderItemMapper.toEntityList(orderDto.getItems());
    itemsToSave.forEach(item -> item.setOrderId(savedOrder.getId()));
    orderItemDao.saveAll(itemsToSave);

    List<OrderItem> savedItems = orderItemDao.findByOrderId(savedOrder.getId());
    savedOrder.setItems(savedItems);

    BigDecimal totalAmount = calculateTotalAMount(itemsToSave);
    OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(
            String.valueOf(savedOrder.getId()),
            String.valueOf(savedOrder.getUserId()),
            totalAmount
    );
    log.info("Sending OrderCreatedEvent for orderId {}", savedOrder.getId());
    kafkaProducerService.sendOrderCreatedEvent(orderCreatedEvent);

    return enrichResponse(savedOrder);
  }

  @Override
  public OrderResponseDTO getOrderById(Long id) {
    Order order = orderDao.findById(id)
        .orElseThrow(() -> new OrderNotFoundException(id));

    return enrichResponse(order);
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

  @Override
  @Transactional
  public void updateOrderStatusAfterPayment(Long orderId, String paymentStatus) {
      log.info("Received payment status: {} for orderId: {}", paymentStatus, orderId);
      Order order = orderDao.findById(orderId).orElseThrow(
              () -> new OrderNotFoundException(orderId)
      );

      if ("SUCCESS".equalsIgnoreCase(paymentStatus)) {
          order.setStatus(OrderStatus.PROCESSING);
          log.info("Setting status to PROCESSING for orderId {}", orderId);
      } else {
          order.setStatus(OrderStatus.CANCELLED);
          log.info("Setting status to CANCELLED for orderId {}", orderId);
      }
      orderDao.update(order);
  }

  private BigDecimal calculateTotalAMount(List<OrderItem> items) {
      List<Long> itemIds = items.stream().map(OrderItem::getItemId).toList();

      if (itemIds.isEmpty()) {
          return BigDecimal.ZERO;
      }

      List<Item> itemsFromDb = itemDao.findByIds(itemIds);
      Map<Long, BigDecimal> priceMap = itemsFromDb.stream()
              .collect(Collectors.toMap(Item::getId, Item::getPrice));

      BigDecimal totalAmount = BigDecimal.ZERO;
      for (OrderItem item : items) {
          BigDecimal price = priceMap.get(item.getItemId());
          BigDecimal amount = new BigDecimal(item.getQuantity());
          BigDecimal total = price.multiply(amount);
          totalAmount = totalAmount.add(total);
      }
      return totalAmount;
  }
}
