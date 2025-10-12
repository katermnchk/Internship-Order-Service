package com.innowise.internship.controller;


import com.innowise.internship.dto.ItemDTO;
import com.innowise.internship.entity.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class ItemControllerTest extends AbstractIntegrationTest {

  private static final String BASE_URL = "/api/v1/items";

  @BeforeEach
  void cleanDatabase() {
    jdbcTemplate.update("DELETE FROM order_items");
    jdbcTemplate.update("DELETE FROM orders");
    jdbcTemplate.update("DELETE FROM items");
  }

  private ItemDTO createItemDto(Long id, String name, BigDecimal price) {
    return new ItemDTO(id, name, price);
  }

  private Item saveItem(String name, BigDecimal price) {
    jdbcTemplate.update("INSERT INTO items (name, price) VALUES (?, ?)", name, price);
    Long id = jdbcTemplate.queryForObject("SELECT id FROM items WHERE name = ?", Long.class, name);
    return new Item(id, name, price.setScale(2));
  }

  @Nested
  class CreateItemTests {

    @Test
    void givenValidRequest_whenCreateItem_thenItemIsCreated() throws Exception {
      ItemDTO request = createItemDto(null, "Laptop", BigDecimal.valueOf(1200.50));

      mockMvc.perform(post(BASE_URL)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated())
          .andExpectAll(
              jsonPath("$.status", is(201)),
              jsonPath("$.message", is("Item created successfully")),
              jsonPath("$.data.id", notNullValue()),
              jsonPath("$.data.name", is("Laptop")),
              jsonPath("$.data.price", is(1200.50))
          );

      Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM items", Integer.class);
      assertEquals(1, count);
    }

    private static Stream<Arguments> invalidItems() {
      return Stream.of(
          Arguments.of(new ItemDTO(null, "", BigDecimal.valueOf(10)),
              List.of("name: Item name cannot be empty")),
          Arguments.of(new ItemDTO(null, null, BigDecimal.valueOf(10)),
              List.of("name: Item name cannot be empty")),
          Arguments.of(new ItemDTO(null, "Phone", null),
              List.of("price: Price must be provided")),
          Arguments.of(new ItemDTO(null, "Phone", BigDecimal.valueOf(-5)),
              List.of("price: Price must be positive"))
      );
    }

    @ParameterizedTest
    @MethodSource("invalidItems")
    void givenInvalidRequest_whenCreateItem_thenBadRequest(ItemDTO invalidDto, List<String> expectedErrors)
        throws Exception {
      mockMvc.perform(post(BASE_URL)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(invalidDto)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.error", is("BAD_REQUEST")))
          .andExpect(jsonPath("$.message", containsInAnyOrder(expectedErrors.toArray())));
    }
  }

  @Nested
  class GetItemTests {

    private Item savedItem1;
    private Item savedItem2;

    @BeforeEach
    void setUp() {
      savedItem1 = saveItem("Book", BigDecimal.valueOf(30));
      savedItem2 = saveItem("Table", BigDecimal.valueOf(200));
    }

    @Test
    void givenExistingId_whenGetItemById_thenReturnItem() throws Exception {
      mockMvc.perform(get(BASE_URL + "/{id}", savedItem1.getId())
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpectAll(
              jsonPath("$.status", is(200)),
              jsonPath("$.message", is("Item fetched successfully")),
              jsonPath("$.data.id", is(savedItem1.getId().intValue())),
              jsonPath("$.data.name", is("Book")),
              jsonPath("$.data.price", is(30.0))
          );
    }

    @Test
    void givenNonExistingId_whenGetItemById_thenNotFound() throws Exception {
      mockMvc.perform(get(BASE_URL + "/{id}", 999L)
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isNotFound());
    }

    @Test
    void givenItemsExist_whenGetAllItems_thenReturnList() throws Exception {
      mockMvc.perform(get(BASE_URL)
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.status", is(200)))
          .andExpect(jsonPath("$.message", is("Items retrieved successfully")))
          .andExpect(jsonPath("$.data", hasSize(2)))
          .andExpect(jsonPath("$.data[*].name", hasItems("Book", "Table")));
    }
  }

  @Nested
  class UpdateItemTests {

    private Item existingItem;

    @BeforeEach
    void setUp() {
      existingItem = saveItem("Pen", BigDecimal.valueOf(5));
    }

    @Test
    void givenValidRequest_whenUpdateItem_thenItemIsUpdated() throws Exception {
      ItemDTO updateRequest = createItemDto(null, "Pen", BigDecimal.valueOf(15));

      mockMvc.perform(patch(BASE_URL + "/{id}", existingItem.getId())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(updateRequest)))
          .andExpect(status().isOk())
          .andExpectAll(
              jsonPath("$.status", is(200)),
              jsonPath("$.message", is("Item updated successfully")),
              jsonPath("$.data.name", is("Pen")),
              jsonPath("$.data.price", is(15))
          );

      Item updatedItem = jdbcTemplate.queryForObject(
          "SELECT * FROM items WHERE id = ?",
          (rs, rowNum) -> new Item(rs.getLong("id"), rs.getString("name"),
              rs.getBigDecimal("price")),
          existingItem.getId()
      );

      assertAll(
          () -> assertEquals("Pen", updatedItem.getName(), "Item name should match"),
          () -> assertTrue(
              new BigDecimal("15.0").compareTo(updatedItem.getPrice()) == 0,
              "Item price should match expected value"
          ));
    }

    @Test
    void givenNonExistingId_whenUpdateItem_thenNotFound() throws Exception {
      ItemDTO request = createItemDto(null, "Ghost", BigDecimal.valueOf(100));
      mockMvc.perform(patch(BASE_URL + "/{id}", 999L)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  class DeleteItemTests {

    private Item itemToDelete;

    @BeforeEach
    void setUp() {
      itemToDelete = saveItem("Chair", BigDecimal.valueOf(50));
    }

    @Test
    void givenExistingId_whenDeleteItem_thenItemDeleted() throws Exception {
      mockMvc.perform(delete(BASE_URL + "/{id}", itemToDelete.getId())
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isNoContent());

      Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM items WHERE id = ?",
          Integer.class, itemToDelete.getId());
      assertEquals(0, count);
    }

    @Test
    void givenNonExistingId_whenDeleteItem_thenNotFound() throws Exception {
      mockMvc.perform(delete(BASE_URL + "/{id}", 999L)
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isNotFound());
    }
  }
}
