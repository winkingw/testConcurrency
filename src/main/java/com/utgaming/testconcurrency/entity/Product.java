package com.utgaming.testconcurrency.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    private BigDecimal price;

    private Integer stock;

    private Integer categoryId;

    private LocalDateTime updatedAt;
}
