package com.innowise.internship.mapper;

import com.innowise.internship.dto.ItemDTO;
import com.innowise.internship.entity.Item;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ItemMapper {

  @Mapping(target = "id", ignore = true)
  Item toEntity(ItemDTO dto);

  ItemDTO toDto(Item entity);

  List<Item> toEntityList(List<ItemDTO> dtoList);
  List<ItemDTO> toDtoList(List<Item> entityList);
}