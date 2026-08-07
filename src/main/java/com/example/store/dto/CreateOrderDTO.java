package com.example.store.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CreateOrderDTO {
    @NotNull(message = "Order's customer ID is required") private Long customerId;

    @NotNull(message = "Order description is required") private String description;

    @NotEmpty(message = "An order must contain at least one product ID")
    private List<Long> productIds;
}
