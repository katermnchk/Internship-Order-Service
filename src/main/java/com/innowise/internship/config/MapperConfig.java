package com.innowise.internship.config;

import com.innowise.internship.mapper.rowMapper.ItemRowMapper;
import com.innowise.internship.mapper.rowMapper.OrderItemRowMapper;
import com.innowise.internship.mapper.rowMapper.OrderRowMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {

  @Bean
  public ItemRowMapper itemRowMapper() {
    return new ItemRowMapper();
  }

  @Bean
  public OrderRowMapper orderMapper() {
    return new OrderRowMapper();
  }

  @Bean
  public OrderItemRowMapper orderItemRowMapper() {
    return new OrderItemRowMapper();
  }

}
