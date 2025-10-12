package com.innowise.internship.controller;

import com.innowise.internship.dto.OrderCreateRequestDTO;
import com.innowise.internship.dto.OrderItemDTO;
import com.innowise.internship.dto.UserDTO;
import com.innowise.internship.entity.Order;
import com.innowise.internship.entity.OrderItem;
import com.innowise.internship.entity.OrderStatus;
import com.innowise.internship.service.UserServiceClient;
import java.time.LocalDate;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class OrderControllerTest extends AbstractIntegrationTest {

  private static final String BASE_URL = "/api/v1/orders";
  private static final Long TEST_USER_ID = 101L;
  private static final Long TEST_ITEM_ID_A = 50L;
  private static final Long TEST_ITEM_ID_B = 51L;

  @MockitoBean
  private UserServiceClient userServiceClient;

  private final UserDTO fakeUser = new UserDTO(
      TEST_USER_ID,
      "Test",
      "User",
      LocalDate.of(2000, 1, 1),
      "test@example.com", Collections.emptyList()
  );

  @BeforeEach
  void cleanDatabase() {
    jdbcTemplate.update("DELETE FROM order_items");
    jdbcTemplate.update("DELETE FROM orders");
    jdbcTemplate.update("DELETE FROM items");

    jdbcTemplate.update("INSERT INTO items (id, name, price) VALUES (?, ?, ?)",
        TEST_ITEM_ID_A, "Item A", 10);
    jdbcTemplate.update("INSERT INTO items (id, name, price) VALUES (?, ?, ?)",
        TEST_ITEM_ID_B, "Item B", 20);
  }

  private OrderItemDTO createOrderItemDto(Long itemId, Integer quantity) {
    return new OrderItemDTO(null, itemId, quantity);
  }

  private List<OrderItemDTO> createValidItems() {
    return List.of(
        createOrderItemDto(TEST_ITEM_ID_A, 2),
        createOrderItemDto(TEST_ITEM_ID_B, 1)
    );
  }

  private OrderCreateRequestDTO createRequestDto(
      Long userId,
      String status,
      List<OrderItemDTO> items
  ) {
    return new OrderCreateRequestDTO(userId, status, items);
  }

  private Order createAndSaveOrder(Long userId, OrderStatus status) {
    Order order = new Order();
    order.setUserId(userId);
    order.setStatus(status);
    Order savedOrder = orderDao.save(order);

    OrderItem item = new OrderItem();
    item.setOrderId(savedOrder.getId());
    item.setItemId(TEST_ITEM_ID_A);
    item.setQuantity(2);

    orderItemDao.saveAll(List.of(item));

    return orderDao.findById(savedOrder.getId()).get();
  }

  @Nested
  class CreateOrderTests {

    @Test
    void givenValidRequest_whenCreateOrder_thenOrderIsCreated() throws Exception {
      when(userServiceClient.getUserById(anyLong())).thenReturn(fakeUser);

      OrderCreateRequestDTO request = createRequestDto(
          TEST_USER_ID,
          OrderStatus.NEW.name(),
          createValidItems()
      );

      mockMvc.perform(post(BASE_URL)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated())
          .andExpectAll(
              jsonPath("$.status", is(201)),
              jsonPath("$.message", is("Order created successfully")),
              jsonPath("$.data.id", notNullValue()),
              jsonPath("$.data.userId", is(TEST_USER_ID.intValue())),
              jsonPath("$.data.status", is(OrderStatus.NEW.name())),
              jsonPath("$.data.items", hasSize(2))
          );
    }

    private static Stream<Arguments> provideInvalidRequests() {
      List<OrderItemDTO> validItems = List.of(new OrderItemDTO(null, TEST_ITEM_ID_A, 1));
      String validStatus = OrderStatus.NEW.name();

      return Stream.of(

          Arguments.of(
              new OrderCreateRequestDTO(null, validStatus, validItems),
              List.of("userId: User ID must not be null")
          ),

          Arguments.of(
              new OrderCreateRequestDTO(TEST_USER_ID, "", validItems),
              List.of("status: Status cannot be empty")
          ),

          Arguments.of(
              new OrderCreateRequestDTO(TEST_USER_ID, validStatus, null),
              List.of("items: Order items list cannot be null")
          ),

          Arguments.of(
              new OrderCreateRequestDTO(TEST_USER_ID, validStatus, new ArrayList<>()),
              List.of("items: Order must contain at least one item")
          ),

          Arguments.of(
              new OrderCreateRequestDTO(TEST_USER_ID, validStatus,
                  List.of(new OrderItemDTO(null, null, 5))),
              List.of("items[0].itemId: Item ID cannot be null")
          ),

          Arguments.of(
              new OrderCreateRequestDTO(TEST_USER_ID, validStatus,
                  List.of(new OrderItemDTO(null, TEST_ITEM_ID_A, 0))),
              List.of("items[0].quantity: Quantity must be at least 1")
          )
      );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidRequests")
    void givenInvalidRequest_whenCreateOrder_thenBadRequest(
        OrderCreateRequestDTO invalidDto, List<String> expectedErrors
    ) throws Exception {

      mockMvc.perform(post(BASE_URL)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(invalidDto)))
          .andExpect(status().isBadRequest())
          .andExpectAll(
              jsonPath("$.error", is("BAD_REQUEST")),
              jsonPath("$.message", containsInAnyOrder(expectedErrors.toArray()))
          );
    }
  }

  @Nested
  class GetOrderTests {

    private Order newOrder;
    private Order processingOrder;
    private Order completedOrder;

    @BeforeEach
    void setUp() {
      newOrder = createAndSaveOrder(101L, OrderStatus.NEW);
      processingOrder = createAndSaveOrder(102L, OrderStatus.PROCESSING);
      completedOrder = createAndSaveOrder(103L, OrderStatus.COMPLETED);
    }

    @Test
    void givenExistingId_whenGetOrderById_thenOrderIsReturned() throws Exception {
      when(userServiceClient.getUserById(anyLong())).thenReturn(fakeUser);

      mockMvc.perform(get(BASE_URL + "/{id}", newOrder.getId())
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpectAll(
              jsonPath("$.status", is(200)),
              jsonPath("$.message", is("Order fetched successfully")),
              jsonPath("$.data.id", is(newOrder.getId().intValue())),
              jsonPath("$.data.userId", is(newOrder.getUserId().intValue())),
              jsonPath("$.data.status", is(OrderStatus.NEW.name())),
              jsonPath("$.data.creationDate", notNullValue()),
              jsonPath("$.data.userInfo", notNullValue()),
              jsonPath("$.data.items", hasSize(1))
          );
    }

    @Test
    void givenNonExistingId_whenGetOrderById_thenNotFound() throws Exception {
      Long nonExistentId = 999L;
      mockMvc.perform(get(BASE_URL + "/{id}", nonExistentId)
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isNotFound());
    }

    @Test
    void givenValidStatuses_whenGetOrdersByStatuses_thenReturnFilteredList() throws Exception {
      String statusesParam = "NEW,PROCESSING";

      mockMvc.perform(get(BASE_URL + "/statuses")
              .param("statuses", statusesParam)
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpectAll(
              jsonPath("$.status", is(200)),
              jsonPath("$.message", is("Orders retrieved successfully by status")),
              jsonPath("$.data", hasSize(2)),
              jsonPath("$.data[*].status", hasItem(OrderStatus.NEW.name())),
              jsonPath("$.data[*].status", hasItem(OrderStatus.PROCESSING.name()))
          );
    }
  }


  @Nested
  class UpdateOrderTests {

    private Order existingOrder;

    @BeforeEach
    void setUp() {
      existingOrder = createAndSaveOrder(TEST_USER_ID, OrderStatus.NEW);
    }

    @Test
    void givenValidRequest_whenUpdateOrder_thenOrderIsUpdated() throws Exception {
      Long orderId = existingOrder.getId();
      OrderCreateRequestDTO updateRequest = createRequestDto(
          TEST_USER_ID,
          OrderStatus.PROCESSING.name(),
          List.of(createOrderItemDto(TEST_ITEM_ID_A, 5))
      );


      mockMvc.perform(patch(BASE_URL + "/{id}", orderId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(updateRequest)))
          .andExpect(status().isOk())
          .andExpectAll(
              jsonPath("$.status", is(200)),
              jsonPath("$.message", is("Order updated successfully")),
              jsonPath("$.data.id", is(orderId.intValue())),
              jsonPath("$.data.status", is(OrderStatus.PROCESSING.name())),
              jsonPath("$.data.items", hasSize(1))
          );


      Order updatedOrder = orderDao.findById(orderId).orElseThrow();
      List<OrderItem> items = orderItemDao.findByOrderId(orderId);

      assertAll(
          () -> assertEquals(OrderStatus.PROCESSING, updatedOrder.getStatus(), "Status in DB should be updated"),
          () -> assertEquals(1, items.size(), "Item count in DB should be updated"),
          () -> assertEquals(5, items.get(0).getQuantity(), "Item quantity in DB should be updated")
      );
    }

    @Test
    void givenNonExistingId_whenUpdateOrder_thenNotFound() throws Exception {
      Long nonExistentId = 999L;
      OrderCreateRequestDTO updateRequest =
          createRequestDto(TEST_USER_ID, OrderStatus.NEW.name(), createValidItems());

      mockMvc.perform(patch(BASE_URL + "/{id}", nonExistentId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(updateRequest)))
          .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @MethodSource("com.innowise.internship.controller.OrderControllerTest$CreateOrderTests#provideInvalidRequests")
    void givenInvalidRequest_whenUpdateOrder_thenBadRequest(
        OrderCreateRequestDTO invalidDto, List<String> expectedErrors
    ) throws Exception {
      mockMvc.perform(patch(BASE_URL + "/{id}", existingOrder.getId())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(invalidDto)))
          .andExpect(status().isBadRequest())
          .andExpectAll(
              jsonPath("$.error", is("BAD_REQUEST")),
              jsonPath("$.message", containsInAnyOrder(expectedErrors.toArray()))
          );
    }
  }

  @Nested
  class DeleteOrderTests {

    private Order orderToDelete;

    @BeforeEach
    void setUp() {
      orderToDelete = createAndSaveOrder(TEST_USER_ID, OrderStatus.NEW);
      assertTrue(orderDao.findById(orderToDelete.getId()).isPresent());
    }

    @Test
    void givenExistingId_whenDeleteOrderById_thenOrderAndItemsAreDeleted() throws Exception {
      Long id = orderToDelete.getId();

      mockMvc.perform(delete(BASE_URL + "/{id}", id)
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isNoContent());


      assertAll("Database deletion check",
          () -> assertFalse(orderDao.findById(id).isPresent(), "Order should be deleted from DB"),
          () -> assertTrue(orderItemDao.findByOrderId(id).isEmpty(), "Order items should be deleted from DB")
      );
    }

    @Test
    void givenNonExistingId_whenDeleteOrderById_thenNotFound() throws Exception {
      Long nonExistentId = 999L;

      mockMvc.perform(delete(BASE_URL + "/{id}", nonExistentId)
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isNotFound());
    }
  }
}