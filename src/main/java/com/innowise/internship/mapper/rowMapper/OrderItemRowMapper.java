package com.innowise.internship.mapper.rowMapper;

import com.innowise.internship.entity.OrderItem;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class OrderItemRowMapper implements RowMapper<OrderItem> {

  @Override
  public OrderItem mapRow(ResultSet rs, int rowNum) throws SQLException {
    OrderItem orderItem = new OrderItem();
    orderItem.setId(rs.getLong("id"));
    orderItem.setOrderId(rs.getLong("order_id"));
    orderItem.setItemId(rs.getLong("item_id"));
    orderItem.setQuantity(rs.getInt("quantity"));
    return orderItem;
  }

}
