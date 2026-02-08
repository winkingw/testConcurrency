package com.utgaming.testconcurrency.service.impl;

import com.utgaming.testconcurrency.service.IdCheckStrategy;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("bloomFilterStrategy")
public class BloomFilterStrategy implements IdCheckStrategy {
    public BloomFilterStrategy(RedissonClient redissonClient) {
        this.redisson = redissonClient;
    }

    private final RedissonClient redisson;

    @Value("${app.idcheck.bloom.key:product:bloom}")
    private String bloomName;

    @Value("${app.idcheck.bloom.size:1000000}")
    private long bloomSize;

    @Value("${app.idcheck.bloom.fpp:0.01}")
    private double fpp;

    @Override
    public boolean exists(Long id) {
        RBloomFilter<Long> bloom = redisson.getBloomFilter(bloomName);
        return bloom.contains(id);
    }

    public void add(Long id) {
        RBloomFilter<Long> bloom = redisson.getBloomFilter(bloomName);
        bloom.add(id);
    }

    public void initIfNeeded(){
        RBloomFilter<Long> bloom = redisson.getBloomFilter(bloomName);
        if(!bloom.isExists()){
            bloom.tryInit(bloomSize,fpp);
        }
    }
}
