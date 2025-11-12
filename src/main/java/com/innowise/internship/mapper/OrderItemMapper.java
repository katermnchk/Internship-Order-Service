package com.innowise.internship.mapper;

import com.innowise.internship.dto.OrderItemDTO;
import com.innowise.internship.entity.OrderItem;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderItemMapper {

  @Mapping(target = "orderId", ignore = true)
  OrderItem toEntity(OrderItemDTO dto);

  OrderItemDTO toDto(OrderItem entity);

  List<OrderItem> toEntityList(List<OrderItemDTO> dtoList);
  List<OrderItemDTO> toDtoList(List<OrderItem> entityList);

}
