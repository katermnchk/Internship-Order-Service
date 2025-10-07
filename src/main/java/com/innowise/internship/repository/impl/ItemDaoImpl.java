package com.innowise.internship.repository.impl;

import com.innowise.internship.entity.Item;
import com.innowise.internship.mapper.ItemRowMapper;
import com.innowise.internship.repository.ItemDao;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ItemDaoImpl implements ItemDao {

  private final JdbcTemplate jdbcTemplate;
  private final ItemRowMapper itemRowMapper;

  private static final class SQL {

    static final String GET_ITEM_BY_ID = """
        SELECT id, name, price
        FROM items
        WHERE id = ?
        """;

    static final String GET_ITEMS_BY_IDS = """
        SELECT id, name, price
        FROM items
        WHERE id IN (%s)
        """;

  }

  @Override
  public Optional<Item> findById(Long id) {
    return jdbcTemplate.query(
        SQL.GET_ITEM_BY_ID,
        itemRowMapper,
        id
    ).stream().findFirst();
  }

  @Override
  public List<Item> findByIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }

    String placeholders = ids.stream()
        .map(id -> "?")
        .collect(Collectors.joining(", "));

    String sql = String.format(SQL.GET_ITEMS_BY_IDS, placeholders);
    return jdbcTemplate.query(sql, itemRowMapper, ids.toArray());
  }
}
