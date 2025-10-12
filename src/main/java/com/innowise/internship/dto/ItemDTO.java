package com.innowise.internship.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ItemDTO(
    Long id,

    @NotBlank(message = "Item name cannot be empty")
    String name,

    @NotNull(message = "Price must be provided")
    @DecimalMin(value = "0.01", message = "Price must be positive")
    BigDecimal price
) {

}