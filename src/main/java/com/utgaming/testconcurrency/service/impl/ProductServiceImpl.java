package com.utgaming.testconcurrency.service.impl;

import com.utgaming.testconcurrency.common.BusinessException;
import com.utgaming.testconcurrency.controller.dto.ProductCreateRequest;
import com.utgaming.testconcurrency.controller.dto.ProductUpdateRequest;
import com.utgaming.testconcurrency.entity.Product;
import com.utgaming.testconcurrency.mapper.ProductMapper;
import com.utgaming.testconcurrency.service.ProductService;
import com.utgaming.testconcurrency.util.RedisUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductMapper productMapper;
    private final RedisUtil redisUtil;
    private final ObjectMapper objectMapper;
    private final boolean cacheEnabled;
    private static final long TTL_SECONDS = 1800;

    public ProductServiceImpl(
            ProductMapper productMapper,
            RedisUtil redisUtil,
            ObjectMapper objectMapper,
            @Value("${app.cache.enabled:true}") boolean cacheEnabled
    ) {
        this.productMapper = productMapper;
        this.redisUtil = redisUtil;
        this.objectMapper = objectMapper;
        this.cacheEnabled = cacheEnabled;
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        String key = "product:" + id;
        if (!cacheEnabled) {
            return productMapper.selectById(id);
        }
        Object cache = redisUtil.get(key);

        if (cache != null) {
            if (RedisUtil.NULL_PLACEHOLDER.equals(cache)) return null;
            if (cache instanceof Product) return (Product) cache;
            return objectMapper.convertValue(cache, Product.class);
        }

        Product db = productMapper.selectById(id);

        if (db == null) {
            redisUtil.set(key, RedisUtil.NULL_PLACEHOLDER,30);
            return null;
        }

        redisUtil.set(key, db, TTL_SECONDS);

        return db;
    }

    @Override
    @Transactional
    public Product createProduct(ProductCreateRequest req) {
        Product product = new Product();
        product.setName(req.getName());
        product.setPrice(req.getPrice());
        product.setCategoryId(req.getCategoryId());
        product.setStock(req.getStock());
        product.setUpdatedAt(LocalDateTime.now());

        int rows = productMapper.insert(product);
        if (rows != 1) {
            throw new BusinessException(500,"创建失败");
        }

        if(cacheEnabled){
            String key = "product:" + product.getId();
            redisUtil.del(key);
        }

        return product;
    }

    @Override
    @Transactional
    public Product updateProduct(ProductUpdateRequest request, Long id) {
        Product p = productMapper.selectById(id);

        if(p == null) throw new BusinessException(404,"没有对应产品");

        if(request.getName()!=null) p.setName(request.getName());
        if(request.getPrice()!=null) p.setPrice(request.getPrice());
        if(request.getCategoryId()!=null) p.setCategoryId(request.getCategoryId());
        if(request.getStock()!=null) p.setStock(request.getStock());

        p.setUpdatedAt(LocalDateTime.now());

        int rows = productMapper.updateById(p);

        if (rows != 1) {
            throw new BusinessException(500,"更新失败");
        }

        if(cacheEnabled){
            String key = "product:" + p.getId();
            redisUtil.del(key);
        }

        return p;
    }

    @Override
    @Transactional
    public Product deleteProduct(Long id) {
        Product p = productMapper.selectById(id);

        if(p == null) throw new BusinessException(404,"没有对应产品");

        int rows = productMapper.deleteById(id);
        if (rows != 1) {
            throw new BusinessException(500, "删除失败");
        }

        if(cacheEnabled){
            String key = "product:" + p.getId();
            redisUtil.del(key);
        }

        return p;
    }
}
