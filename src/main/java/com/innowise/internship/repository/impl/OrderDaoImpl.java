package com.innowise.internship.repository.impl;

import com.innowise.internship.entity.Order;
import com.innowise.internship.entity.OrderStatus;
import com.innowise.internship.mapper.OrderRowMapper;
import com.innowise.internship.repository.OrderDao;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderDaoImpl implements OrderDao {

  private final JdbcTemplate jdbcTemplate;
  private final OrderRowMapper orderRowMapper;

  private static final class SQL {

    static final String SELECT_ALL = """
        SELECT id, user_id, status, creation_date
        FROM orders
        """;

    static final String INSERT_ORDER = """
        INSERT INTO orders (user_id, status, creation_date)
        VALUES (?, ?, CURRENT_TIMESTAMP)
        """;

    static final String GET_ORDER_BY_ID = SELECT_ALL + """
        WHERE id = ?
        """;

    static final String GET_ORDERS_BY_IDS = SELECT_ALL + """
        WHERE id IN (%s)
        """;

    static final String GET_ORDERS_BY_STATUSES = SELECT_ALL + """
        WHERE status IN (%s)
        """;

    static final String UPDATE_ORDER = """
        UPDATE orders
        SET user_id = ?, status = ?
        WHERE id = ?
        """;

    static final String DELETE_ORDER = """
        DELETE FROM orders
        WHERE id = ?
        """;
  }

  @Override
  public Order save(Order order) {
    KeyHolder keyHolder = new GeneratedKeyHolder();

    jdbcTemplate.update(connection -> {
      PreparedStatement ps = connection.prepareStatement(SQL.INSERT_ORDER, new String[]{"id"});
      ps.setLong(1, order.getUserId());
      ps.setString(2, order.getStatus().name());
      return ps;
    }, keyHolder);

    if (keyHolder.getKey() != null) {
      order.setId(keyHolder.getKey().longValue());
    }

    return order;
  }

  @Override
  public Optional<Order> findById(Long id) {
    return jdbcTemplate.query(
        SQL.GET_ORDER_BY_ID,
        orderRowMapper,
        id
    ).stream().findFirst();
  }

  @Override
  public List<Order> findByIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }

    String placeholders = ids.stream()
        .map(id -> "?")
        .collect(Collectors.joining(", "));

    String sql = String.format(SQL.GET_ORDERS_BY_IDS, placeholders);
    return jdbcTemplate.query(sql, orderRowMapper, ids.toArray());
  }

  @Override
  public List<Order> findByStatuses(List<OrderStatus> statuses) {
    if (statuses == null || statuses.isEmpty()) {
      return List.of();
    }

    String placeholders = statuses.stream()
        .map(s -> "?")
        .collect(Collectors.joining(", "));

    String sql = String.format(SQL.GET_ORDERS_BY_STATUSES, placeholders);
    Object[] statusNames = statuses.stream()
        .map(OrderStatus::name)
        .toArray();

    return jdbcTemplate.query(sql, orderRowMapper, statusNames);
  }

  @Override
  public int update(Order order) {
    return jdbcTemplate.update(
        SQL.UPDATE_ORDER,
        order.getUserId(),
        order.getStatus().name(),
        order.getId()
    );
  }

  @Override
  public int delete(Long id) {
    return jdbcTemplate.update(SQL.DELETE_ORDER, id);
  }
}
