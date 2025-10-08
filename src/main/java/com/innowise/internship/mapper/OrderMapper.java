package com.innowise.internship.mapper;

import com.innowise.internship.dto.OrderCreateRequestDTO;
import com.innowise.internship.dto.OrderResponseDTO;
import com.innowise.internship.entity.Order;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = OrderItemMapper.class)
public interface OrderMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "creationDate", ignore = true)
  Order toEntity(OrderCreateRequestDTO dto);

  @Mapping(target = "userInfo", ignore = true)
  OrderResponseDTO toResponseDto(Order entity);

  List<OrderResponseDTO> toResponseDtoList(List<Order> entityList);

}
