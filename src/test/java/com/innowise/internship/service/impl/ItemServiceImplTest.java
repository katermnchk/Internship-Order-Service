package com.innowise.internship.service.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.innowise.internship.dto.ItemDTO;
import com.innowise.internship.entity.Item;
import com.innowise.internship.exception.ItemNotFoundException;
import com.innowise.internship.mapper.ItemMapper;
import com.innowise.internship.repository.ItemDao;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

  @Mock
  private ItemDao itemDao;

  @Mock
  private ItemMapper itemMapper;

  @InjectMocks
  private ItemServiceImpl itemService;

  private Item item;
  private ItemDTO itemDto;
  private final Long ITEM_ID = 1L;
  private final Long INVALID_ITEM_ID = 99L;

  @BeforeEach
  void setUp() {
    item = new Item(ITEM_ID, "Test", new BigDecimal("10.0"));
    itemDto = new ItemDTO(ITEM_ID, "Test", new BigDecimal("10.0"));
  }

  @Test
  void givenValidDto_whenCreateItem_thenItemIsSaved() {
    ItemDTO newDto = new ItemDTO(null, "New item", new BigDecimal("5.00"));
    Item newEntity = new Item(null, "New item", new BigDecimal("5.00"));
    Item savedEntity = new Item(2L, "New item", new BigDecimal("5.00"));

    when(itemMapper.toEntity(newDto)).thenReturn(newEntity);
    when(itemDao.save(newEntity)).thenReturn(savedEntity);
    when(itemMapper.toDto(savedEntity)).thenReturn(newDto);

    ItemDTO result = itemService.createItem(newDto);

    assertAll(
        () -> assertEquals(newDto, result),
        () -> verify(itemDao).save(newEntity)
    );
  }

  @Test
  void givenExistingId_whenGetItemById_thenReturnItemDTO() {
    when(itemDao.findById(ITEM_ID)).thenReturn(Optional.of(item));
    when(itemMapper.toDto(item)).thenReturn(itemDto);

    ItemDTO result = itemService.getItemById(ITEM_ID);

    assertAll(
        () -> assertNotNull(result),
        () -> assertEquals(ITEM_ID, result.id()),
        () -> verify(itemDao).findById(ITEM_ID)
    );
  }

  @Test
  void givenNonExistingId_whenGetItemById_thenThrowException() {
    when(itemDao.findById(INVALID_ITEM_ID)).thenReturn(Optional.empty());

    assertThrows(ItemNotFoundException.class,
        () -> itemService.getItemById(INVALID_ITEM_ID));
  }

  @Test
  void givenExistingItem_whenUpdateItem_thenReturnUpdatedDTO() {
    when(itemDao.findById(1L)).thenReturn(Optional.of(item));
    when(itemMapper.toEntity(itemDto)).thenReturn(item);
    when(itemDao.update(item)).thenReturn(item);
    when(itemMapper.toDto(item)).thenReturn(itemDto);

    ItemDTO result = itemService.updateItem(1L, itemDto);

    assertAll(
        () -> assertNotNull(result),
        () -> assertEquals("Test", result.name())
    );
  }

  @Test
  void givenNonExistingId_whenUpdateItem_thenThrowException() {
    when(itemDao.findById(INVALID_ITEM_ID)).thenReturn(Optional.empty());

    assertThrows(ItemNotFoundException.class,
        () -> itemService.updateItem(INVALID_ITEM_ID, itemDto));
    verify(itemDao, never()).update(any());
  }

  @Test
  void givenExistingId_whenDeleteItem_thenVerifyDaoDelete() {
    when(itemDao.delete(ITEM_ID)).thenReturn(1);
    itemService.deleteItem(ITEM_ID);
    verify(itemDao).delete(ITEM_ID);
  }

  @Test
  void givenNonExistingId_whenDeleteItem_thenThrowException() {
    when(itemDao.delete(INVALID_ITEM_ID)).thenReturn(0);

    assertThrows(ItemNotFoundException.class,
        () -> itemService.deleteItem(INVALID_ITEM_ID));
    verify(itemDao).delete(INVALID_ITEM_ID);
  }

  @Test
  void givenItemsInDatabase_whenGetAllItems_thenReturnList() {
    when(itemDao.findAll()).thenReturn(List.of(item));
    when(itemMapper.toDto(item)).thenReturn(itemDto);

    List<ItemDTO> result = itemService.getAllItems();

    assertAll(
        () -> assertEquals(1, result.size()),
        () -> assertEquals("Test", result.get(0).name())
    );
  }

}
