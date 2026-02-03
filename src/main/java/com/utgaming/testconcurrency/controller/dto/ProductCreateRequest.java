package com.utgaming.testconcurrency.controller.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductCreateRequest {
    @NotBlank
    private String name;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal price;

    @NotNull
    @Min(0)
    private Integer stock;

    @NotNull
    @Min(1)
    private Integer categoryId;
}
