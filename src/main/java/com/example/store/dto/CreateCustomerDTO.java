package com.example.store.dto;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class CreateCustomerDTO {
    @NotNull(message = "Customer name is required") private String name;
}
