package com.innowise.internship.repository;

import com.innowise.internship.entity.Item;
import java.util.List;
import java.util.Optional;

public interface ItemDao {

  Optional<Item> findById(Long id);
  List<Item> findByIds(List<Long> ids);

}
