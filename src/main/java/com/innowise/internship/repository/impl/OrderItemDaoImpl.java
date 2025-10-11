package com.innowise.internship.repository.impl;

import com.innowise.internship.entity.OrderItem;
import com.innowise.internship.repository.OrderItemDao;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderItemDaoImpl implements OrderItemDao {

  private final JdbcTemplate jdbcTemplate;
  private final RowMapper<OrderItem> orderItemRowMapper;

  private static final class SQL {

    static final String INSERT_ITEM_ORDER = """
        INSERT INTO order_items (order_id, item_id, quantity)
        VALUES (?, ?, ?)
        """;

    static final String SELECT_BY_ORDER_ID = """
        SELECT id, order_id, item_id, quantity
        FROM order_items
        WHERE order_id = ?
        """;

    static final String DELETE_BY_ORDER_ID = """
        DELETE FROM order_items
        WHERE order_id = ?
        """;
  }

  @Override
  public void saveAll(List<OrderItem> items) {
    if (items == null || items.isEmpty()) {
      return;
    }

    jdbcTemplate.batchUpdate(
        SQL.INSERT_ITEM_ORDER,
        items,
        items.size(),
        (ps, item) -> {
          ps.setLong(1, item.getOrderId());
          ps.setLong(2, item.getItemId());
          ps.setInt(3, item.getQuantity());
        }
    );
  }

  @Override
  public List<OrderItem> findByOrderId(Long orderId) {
    return jdbcTemplate.query(
        SQL.SELECT_BY_ORDER_ID,
        orderItemRowMapper,
        orderId
    );
  }

  @Override
  public int deleteByOrderId(Long orderId) {
    return jdbcTemplate.update(
        SQL.DELETE_BY_ORDER_ID,
        orderId
    );
  }
}
