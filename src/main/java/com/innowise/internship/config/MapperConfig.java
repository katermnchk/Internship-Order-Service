package com.innowise.internship.config;

import com.innowise.internship.entity.Item;
import com.innowise.internship.entity.Order;
import com.innowise.internship.entity.OrderItem;
import com.innowise.internship.mapper.rowMapper.ItemRowMapper;
import com.innowise.internship.mapper.rowMapper.OrderItemRowMapper;
import com.innowise.internship.mapper.rowMapper.OrderRowMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.RowMapper;

@Configuration
public class MapperConfig {

  @Bean
  public RowMapper<Item> itemRowMapper() {
    return new ItemRowMapper();
  }

  @Bean
  public RowMapper<Order> orderMapper() {
    return new OrderRowMapper();
  }

  @Bean
  public RowMapper<OrderItem> orderItemRowMapper() {
    return new OrderItemRowMapper();
  }

}
