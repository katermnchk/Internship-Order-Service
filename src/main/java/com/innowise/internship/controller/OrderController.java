package com.innowise.internship.controller;

import com.innowise.internship.dto.OrderCreateRequestDTO;
import com.innowise.internship.dto.OrderResponseDTO;
import com.innowise.internship.dto.response.ApiResponse;
import com.innowise.internship.entity.OrderStatus;
import com.innowise.internship.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${app.api.base-path}/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;

  @PostMapping
  public ResponseEntity<ApiResponse<OrderResponseDTO>> createOrder(
      @RequestBody @Valid OrderCreateRequestDTO requestDTO
  ) {
    OrderResponseDTO responseDTO = orderService.createOrder(requestDTO);
    return ResponseEntity.
        status(HttpStatus.CREATED).
        body(new ApiResponse<>(
            HttpStatus.CREATED.value(),
            "Order created successfully",
            responseDTO
        ));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<OrderResponseDTO>> getOrderById(@PathVariable Long id) {
    OrderResponseDTO responseDTO = orderService.getOrderById(id);
    return ResponseEntity.ok(new ApiResponse<>(
        HttpStatus.OK.value(),
        "Order fetched successfully",
        responseDTO
    ));
  }

  @GetMapping("/ids")
  public ResponseEntity<ApiResponse<List<OrderResponseDTO>>> getOrdersByIds(@RequestParam List<Long> ids) {
    List<OrderResponseDTO> responses = orderService.getOrdersByIds(ids);
    return ResponseEntity.ok(new ApiResponse<>(
        HttpStatus.OK.value(),
        "Orders fetched successfully",
        responses
    ));
  }

  @GetMapping("/statuses")
  public ResponseEntity<ApiResponse<List<OrderResponseDTO>>> getOrdersByStatuses(@RequestParam List<String> statuses) {
    List<OrderStatus> orderStatuses = statuses.stream()
        .map(s -> OrderStatus.valueOf(s.toUpperCase()))
        .collect(Collectors.toList());

    List<OrderResponseDTO> responses = orderService.getOrdersByStatuses(orderStatuses);
    return ResponseEntity.ok(new ApiResponse<>(
        HttpStatus.OK.value(),
        "Orders retrieved successfully by status",
        responses
    ));
  }

  @PatchMapping("/{id}")
  public ResponseEntity<ApiResponse<OrderResponseDTO>> updateOrder(
      @PathVariable Long id,
      @RequestBody @Valid OrderCreateRequestDTO requestDTO
  ) {
    OrderResponseDTO responseDTO = orderService.updateOrder(id, requestDTO);
    return ResponseEntity.ok(new ApiResponse<>(
        HttpStatus.OK.value(),
        "Order updated successfully",
        responseDTO
    ));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteOrderById(@PathVariable Long id) {
    orderService.deleteOrderById(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

}
