package com.utgaming.testconcurrency.service.impl;

import com.utgaming.testconcurrency.mapper.ProductMapper;
import com.utgaming.testconcurrency.service.IdCheckStrategy;
import org.springframework.stereotype.Component;

@Component("dbCheckStrategy")
public class DbCheckStrategy implements IdCheckStrategy {
    private final ProductMapper productMapper;

    public DbCheckStrategy(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    @Override
    public boolean exists(Long id) {
        return productMapper.selectById(id) != null;
    }
}
