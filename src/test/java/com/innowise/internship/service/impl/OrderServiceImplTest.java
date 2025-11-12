package com.innowise.internship.service.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.innowise.internship.dto.OrderCreateRequestDTO;
import com.innowise.internship.dto.OrderResponseDTO;
import com.innowise.internship.dto.UserDTO;
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
import com.innowise.internship.service.UserServiceClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

  @Mock
  private OrderDao orderDao;

  @Mock
  private OrderItemDao orderItemDao;

  @Mock
  private OrderMapper orderMapper;

  @Mock
  private OrderItemMapper orderItemMapper;

  @Mock
  private UserServiceClient userClient;

  @Mock
  private ItemDao itemDao;

  @Mock
  private KafkaProducerService kafkaProducerService;

  @InjectMocks
  private OrderServiceImpl orderService;

  private final Long ORDER_ID = 10L;
  private final Long USER_ID = 1L;
  private final Long INVALID_ID = 99L;
  private Order orderEntity;
  private UserDTO userDto;
  private OrderResponseDTO baseResponseDto;

  @BeforeEach
  void setUp() {
    orderEntity = new Order(ORDER_ID, USER_ID, OrderStatus.NEW, Instant.now(), Collections.emptyList());
    userDto = new UserDTO(USER_ID, "Test", "User", null, "test@example.com",
        Collections.emptyList());

    baseResponseDto = new OrderResponseDTO(
        ORDER_ID,
        USER_ID,
        OrderStatus.NEW,
        Instant.now(),
        userDto,
        Collections.emptyList()
    );

    lenient().when(userClient.getUserById(USER_ID)).thenReturn(userDto);
    lenient().when(orderMapper.toResponseDto(any(Order.class))).thenReturn(baseResponseDto);
  }

  @Test
  void givenValidRequest_whenCreateOrder_thenOrderAndItemsAreSaved() {
    OrderCreateRequestDTO createDto = mock(OrderCreateRequestDTO.class);
    List<OrderItem> items = List.of(new OrderItem(null, null, 1L, 2));

    Item mockItem = new Item(1L, "Test Item", new BigDecimal("10.00"));

    when(orderMapper.toEntity(createDto)).thenReturn(orderEntity);
    when(orderDao.save(orderEntity)).thenReturn(orderEntity);
    when(createDto.getStatus()).thenReturn("new");
    when(orderItemMapper.toEntityList(any())).thenReturn(items);
    when(orderDao.findById(orderEntity.getId())).thenReturn(Optional.of(orderEntity));
    when(userClient.getUserById(anyLong())).thenReturn(mock(UserDTO.class));
    when(orderMapper.toResponseDto(any(Order.class))).thenReturn(mock(OrderResponseDTO.class));
    when(orderItemDao.findByOrderId(anyLong())).thenReturn(items);
    when(itemDao.findByIds(anyList())).thenReturn(List.of(mockItem));

    orderService.createOrder(createDto);

    assertAll(
        () -> verify(orderDao).save(orderEntity),
        () -> verify(orderItemDao).saveAll(anyList()),
        () -> verify(orderMapper).toEntity(createDto)
    );
  }

  @Test
  void givenExistingId_whenGetOrderById_thenReturnsEnrichedDto() {
    when(orderDao.findById(ORDER_ID)).thenReturn(Optional.of(orderEntity));

    OrderResponseDTO result = orderService.getOrderById(ORDER_ID);

    assertAll(
        () -> assertNotNull(result),
        () -> assertEquals(USER_ID, result.getUserId()),
        () -> assertNotNull(result.getUserInfo()),
        () -> verify(userClient).getUserById(USER_ID)
    );
  }

  @Test
  void givenMissingId_whenGetOrderById_thenThrowsNotFoundException() {
    when(orderDao.findById(INVALID_ID)).thenReturn(Optional.empty());

    assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(INVALID_ID));
  }

  @Test
  void givenIdList_whenGetOrdersByIds_thenReturnsListAndEnrichesAll() {
    List<Long> ids = List.of(ORDER_ID);
    List<Order> orders = List.of(orderEntity);
    when(orderDao.findByIds(ids)).thenReturn(orders);

    List<OrderResponseDTO> results = orderService.getOrdersByIds(ids);

    assertAll(
        () -> assertFalse(results.isEmpty()),
        () -> assertEquals(1, results.size()),
        () -> verify(orderDao).findByIds(ids),
        () -> verify(userClient, times(1)).getUserById(USER_ID)
    );
  }

  @ParameterizedTest
  @EnumSource(OrderStatus.class)
  void givenAnyValidStatus_whenGetOrdersByStatuses_thenReturnsListAndEnriches(OrderStatus status) {
    Order orderWithSpecificStatus = new Order(ORDER_ID, USER_ID, status, Instant.now(), null);
    List<OrderStatus> statusList = List.of(status);
    List<Order> orders = List.of(orderWithSpecificStatus);

    when(orderDao.findByStatuses(statusList)).thenReturn(orders);

    OrderResponseDTO dynamicResponse =
        new OrderResponseDTO(ORDER_ID, USER_ID, status, Instant.now(), userDto, Collections.emptyList());
    when(orderMapper.toResponseDto(orderWithSpecificStatus)).thenReturn(dynamicResponse);

    List<OrderResponseDTO> results = orderService.getOrdersByStatuses(statusList);

    assertAll(
        () -> assertFalse(results.isEmpty(), "Result list should not be empty."),
        () -> assertEquals(status, results.get(0).getStatus(),
            "Returned order must have the requested status."),
        () -> verify(orderDao).findByStatuses(statusList)
    );

    verify(userClient, times(1)).getUserById(USER_ID);
  }

  @Test
  void givenExistingIdAndValidDto_whenUpdateOrder_thenHeaderAndItemsAreReplaced() {
    OrderCreateRequestDTO updateDto = mock(OrderCreateRequestDTO.class);
    List<OrderItem> newItems = List.of(new OrderItem(null, null, 3L, 5));

    when(orderDao.findById(ORDER_ID)).thenReturn(Optional.of(orderEntity));
    when(updateDto.getUserId()).thenReturn(2L);
    when(updateDto.getStatus()).thenReturn("SHIPPED");
    when(orderItemMapper.toEntityList(any())).thenReturn(newItems);

    orderService.updateOrder(ORDER_ID, updateDto);

    assertAll(
        () -> verify(orderDao).update(orderEntity),
        () -> verify(orderItemDao).deleteByOrderId(ORDER_ID),
        () -> verify(orderItemDao).saveAll(newItems)
    );
  }

  @Test
  void givenMissingId_whenUpdateOrder_thenThrowsNotFoundException() {
    when(orderDao.findById(INVALID_ID)).thenReturn(Optional.empty());

    assertThrows(OrderNotFoundException.class,
        () -> orderService.updateOrder(INVALID_ID, mock(OrderCreateRequestDTO.class)));
    verify(orderDao, never()).update(any());
  }

  @Test
  void givenExistingId_whenDeleteOrderById_thenOrderAndItemsAreDeleted() {
    when(orderDao.findById(ORDER_ID)).thenReturn(Optional.of(orderEntity));

    orderService.deleteOrderById(ORDER_ID);

    assertAll(
        () -> verify(orderItemDao).deleteByOrderId(ORDER_ID),
        () -> verify(orderDao).delete(ORDER_ID)
    );
  }

  @Test
  void givenMissingId_whenDeleteOrderById_thenThrowsNotFoundException() {
    when(orderDao.findById(INVALID_ID)).thenReturn(Optional.empty());

    assertThrows(OrderNotFoundException.class,
        () -> orderService.deleteOrderById(INVALID_ID));
    verify(orderItemDao, never()).deleteByOrderId(any());
  }
}