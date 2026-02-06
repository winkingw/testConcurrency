package com.utgaming.testconcurrency.service;

import com.utgaming.testconcurrency.entity.Product;
import com.utgaming.testconcurrency.mapper.ProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;


@Slf4j
@Service
public class StockAsyncService {
    private final ProductMapper productMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final Counter asyncSuccess;
    private final Counter asyncFail;
    private final Counter asyncRetry;
    private final Counter asyncRollback;

    public StockAsyncService(ProductMapper productMapper,
                             StringRedisTemplate stringRedisTemplate,
                             MeterRegistry meterRegistry) {
        this.productMapper = productMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.asyncSuccess = meterRegistry.counter("stock.async.success");
        this.asyncFail = meterRegistry.counter("stock.async.fail");
        this.asyncRetry = meterRegistry.counter("stock.async.retry");
        this.asyncRollback = meterRegistry.counter("stock.async.rollback");
    }

    @Async
    public void writeBackAsync(Long id,int count) {
        for(int i=0;i < 3;i++){
            /*int rows = productMapper.deductStock(id, count);
            if(rows == 1){
                log.info("异步回写成功 id={}, count={}", id, count);
                return;
            }*/

            Product p = productMapper.selectById(id);

            if(i > 0) asyncRetry.increment();

            if(p == null){
                asyncFail.increment();
                log.warn("异步回写失败，未找到对应商品 id={}, count={}", id, count);
                return;
            }

            if(p.getStock() < count){
                asyncFail.increment();
                log.warn("异步回写失败，商品数量不足 id={}, count={}", id, count);
                return;
            }

            p.setStock(p.getStock()-count);
            int rows = productMapper.updateById(p);
            if(rows == 1){
                asyncSuccess.increment();
                log.info("异步回写成功 id={}, count={}", id, count);
                return;
            }

            try{
                Thread.sleep(100);
            }  catch (InterruptedException e){
                Thread.currentThread().interrupt();
                log.warn("异步回写线程被中断，停止重试 id={}, count={}", id, count);
                return;
            }
        }

        stringRedisTemplate.opsForValue().increment("stock:"+ id,count);
        log.warn("异步回写失败，回滚redis库存量,id={},count={}", id, count);
        asyncFail.increment();
        asyncRollback.increment();
    }
}
