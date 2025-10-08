package com.innowise.internship.mapper.rowMapper;

import com.innowise.internship.entity.Order;
import com.innowise.internship.entity.OrderStatus;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneOffset;
import org.springframework.jdbc.core.RowMapper;

public class OrderRowMapper implements RowMapper<Order> {

  @Override
  public Order mapRow(ResultSet rs, int rowNum) throws SQLException {
    Order order = new Order();
    order.setId(rs.getLong("id"));
    order.setUserId(rs.getLong("user_id"));
    order.setStatus(OrderStatus.valueOf(rs.getString("status")));
    order.setCreationDate(
        rs.getTimestamp("creation_date").toInstant().atOffset(ZoneOffset.UTC).toInstant()
    );
    order.setItems(null);
    return order;
  }

}
