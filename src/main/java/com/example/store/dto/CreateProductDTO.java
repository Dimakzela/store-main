package com.example.store.dto;

import jakarta.validation.constraints.NotNull;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateProductDTO {
    @NotNull(message = "Product description is required") private String description;
}
