package com.innowise.internship.controller;

import com.innowise.internship.dto.ItemDTO;
import com.innowise.internship.dto.response.ApiResponse;
import com.innowise.internship.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${app.api.base-path}/items")
@RequiredArgsConstructor
public class ItemController {

  private final ItemService itemService;

  @PostMapping
  public ResponseEntity<ApiResponse<ItemDTO>> createItem(
      @RequestBody @Valid ItemDTO requestDto
  ) {
    ItemDTO responseDto = itemService.createItem(requestDto);
    return ResponseEntity.
        status(HttpStatus.CREATED).
        body(new ApiResponse<>(
            HttpStatus.CREATED.value(),
            "Item created successfully",
            responseDto
        ));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<ItemDTO>> getItemById(@PathVariable Long id) {
    ItemDTO responseDto = itemService.getItemById(id);
    return ResponseEntity.ok(new ApiResponse<>(
        HttpStatus.OK.value(),
        "Item fetched successfully",
        responseDto
    ));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<ItemDTO>>> getAllItems() {
    List<ItemDTO> responses = itemService.getAllItems();
    return ResponseEntity.ok(new ApiResponse<>(
        HttpStatus.OK.value(),
        "Items retrieved successfully",
        responses
    ));
  }

  @PatchMapping("/{id}")
  public ResponseEntity<ApiResponse<ItemDTO>> updateItem(
      @PathVariable Long id,
      @RequestBody @Valid ItemDTO requestDto
  ) {
    ItemDTO responseDto = itemService.updateItem(id, requestDto);
    return ResponseEntity.ok(new ApiResponse<>(
        HttpStatus.OK.value(),
        "Item updated successfully",
        responseDto
    ));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
    itemService.deleteItem(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}