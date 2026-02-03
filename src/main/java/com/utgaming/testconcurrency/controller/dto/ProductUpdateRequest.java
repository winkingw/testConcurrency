package com.utgaming.testconcurrency.controller.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductUpdateRequest {
    private String name;

    @DecimalMin("0.01")
    private BigDecimal price;

    @Min(0)
    private Integer stock;

    @Min(1)
    private Integer categoryId;
}
