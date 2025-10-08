package com.innowise.internship.repository.impl;

import com.innowise.internship.entity.Item;
import com.innowise.internship.exception.ItemNotFoundException;
import com.innowise.internship.mapper.rowMapper.ItemRowMapper;
import com.innowise.internship.repository.ItemDao;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ItemDaoImpl implements ItemDao {

  private final JdbcTemplate jdbcTemplate;
  private final ItemRowMapper itemRowMapper;

  private static final class SQL {

    static final String SELECT_BASE = """
        SELECT id, name, price
        FROM items
        """;

    static final String INSERT_ITEM = """
        INSERT INTO items (name, price)
        VALUES (?, ?)
        """;

    static final String UPDATE_ITEM = """
        UPDATE items
        SET name = ?, price = ?
        WHERE id = ?
        """;

    static final String DELETE_ITEM = """
        DELETE FROM items
        WHERE id = ?
        """;

    static final String GET_ITEM_BY_ID = SELECT_BASE + """
        WHERE id = ?
        """;

    static final String GET_ITEMS_BY_IDS = SELECT_BASE + """
        WHERE id IN (%s)
        """;

    static final String GET_ALL_ITEMS = SELECT_BASE;

  }

  @Override
  public Item save(Item item) {
    KeyHolder keyHolder = new GeneratedKeyHolder();
    final String ID_COLUMN_NAME = "id";

    jdbcTemplate.update(connection -> {
      PreparedStatement ps = connection.prepareStatement(
          SQL.INSERT_ITEM,
          new String[]{ID_COLUMN_NAME}
      );
      ps.setString(1, item.getName());
      ps.setBigDecimal(2, item.getPrice());
      return ps;
    }, keyHolder);

    Number key = keyHolder.getKey();

    if (key != null) {
      item.setId(key.longValue());
    } else {
      throw new DataRetrievalFailureException("Failed to retrieve auto-generated ID after INSERT.");
    }

    return item;
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

  @Override
  public List<Item> findAll() {
    return jdbcTemplate.query(SQL.GET_ALL_ITEMS, itemRowMapper);
  }

  @Override
  public Item update(Item item) {
    int updatedRows = jdbcTemplate.update(
        SQL.UPDATE_ITEM,
        item.getName(),
        item.getPrice(),
        item.getId()
    );
    if (updatedRows == 0) {
      throw new ItemNotFoundException(item.getId());
    }
    return item;
  }

  @Override
  public int delete(Long id) {
    return jdbcTemplate.update(SQL.DELETE_ITEM, id);
  }
}