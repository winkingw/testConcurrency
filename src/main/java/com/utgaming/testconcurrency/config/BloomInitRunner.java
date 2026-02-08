package com.utgaming.testconcurrency.config;

import com.utgaming.testconcurrency.entity.Product;
import com.utgaming.testconcurrency.mapper.ProductMapper;
import com.utgaming.testconcurrency.service.impl.BloomFilterStrategy;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BloomInitRunner {
    private final BloomFilterStrategy bloomFilterStrategy;

    public BloomInitRunner(BloomFilterStrategy bloomFilterStrategy) {
        this.bloomFilterStrategy = bloomFilterStrategy;
    }

    @Bean
    public ApplicationRunner initBloom(ProductMapper productMapper){
        return args -> {
            bloomFilterStrategy.initIfNeeded();
            for(Product p : productMapper.selectList(null)){
                bloomFilterStrategy.add(p.getId());
            }
        };
    }
}
