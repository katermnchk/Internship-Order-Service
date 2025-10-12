package com.innowise.internship.service.impl;

import com.innowise.internship.repository.ItemDao;
import com.innowise.internship.dto.ItemDTO;
import com.innowise.internship.entity.Item;
import com.innowise.internship.exception.ItemNotFoundException;
import com.innowise.internship.mapper.ItemMapper;
import com.innowise.internship.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

  private final ItemDao itemDao;
  private final ItemMapper itemMapper;

  @Transactional
  @Override
  public ItemDTO createItem(ItemDTO dto) {
    Item item = itemMapper.toEntity(dto);
    Item savedItem = itemDao.save(item);
    return itemMapper.toDto(savedItem);
  }

  @Override
  public ItemDTO getItemById(Long id) {
    Item item = itemDao.findById(id)
        .orElseThrow(() -> new ItemNotFoundException(id));

    return itemMapper.toDto(item);
  }

  @Override
  public List<ItemDTO> getAllItems() {
    List<Item> items = itemDao.findAll();
    return items.stream()
        .map(itemMapper::toDto)
        .collect(Collectors.toList());
  }

  @Transactional
  @Override
  public ItemDTO updateItem(Long id, ItemDTO dto) {
    itemDao.findById(id)
        .orElseThrow(() -> new ItemNotFoundException(id));

    Item itemToUpdate = itemMapper.toEntity(dto);
    itemToUpdate.setId(id);

    Item updatedItem = itemDao.update(itemToUpdate);

    return itemMapper.toDto(updatedItem);
  }

  @Transactional
  @Override
  public void deleteItem(Long id) {
    int deletedRows = itemDao.delete(id);

    if (deletedRows == 0) {
      throw new ItemNotFoundException(id);
    }
  }
}