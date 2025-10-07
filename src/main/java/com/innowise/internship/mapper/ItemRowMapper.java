package com.innowise.internship.mapper;

import com.innowise.internship.entity.Item;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class ItemRowMapper implements RowMapper<Item> {

  @Override
  public Item mapRow(ResultSet rs, int rowNum) throws SQLException {
    Item item = new Item();
    item.setId(rs.getLong("id"));
    item.setName(rs.getString("name"));
    item.setPrice(rs.getBigDecimal("price"));
    return item;
  }

}
