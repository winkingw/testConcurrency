package com.utgaming.testconcurrency.service.impl;

import com.utgaming.testconcurrency.common.BusinessException;
import com.utgaming.testconcurrency.common.CacheData;
import com.utgaming.testconcurrency.common.StockDeductMessage;
import com.utgaming.testconcurrency.controller.dto.ProductCreateRequest;
import com.utgaming.testconcurrency.controller.dto.ProductUpdateRequest;
import com.utgaming.testconcurrency.entity.Product;
import com.utgaming.testconcurrency.mapper.ProductMapper;
import com.utgaming.testconcurrency.service.ProductService;
import com.utgaming.testconcurrency.service.StockAsyncService;
import com.utgaming.testconcurrency.util.RedisUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.*;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {
    private final ProductMapper productMapper;
    private final RedisUtil redisUtil;
    private final ObjectMapper objectMapper;
    private final boolean cacheEnabled;

    private static final long TTL_SECONDS = 1800;
    private static final long TTL_JITTER_SECONDS = 300;

    private final KafkaTemplate<String, StockDeductMessage> kafkaTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final DefaultRedisScript<Long> deductStockScript;
    private final StockAsyncService stockAsyncService;
    @Value("${app.stock.async.enabled:false}") boolean asyncEnabled;
    @Value("${app.stock.kafka.enabled:false}") boolean kafkaEnabled;

    private final Counter cacheHit;
    private final Counter cacheMiss;
    private final Counter deductSuccess;
    private final Counter deductFail;
    private final Counter dbWriteFail;
    private final Counter asyncFail;
    private final Counter asyncSuccess;
    private final Counter asyncRetry;
    private final Counter asyncRollback;

    public ProductServiceImpl(
            ProductMapper productMapper,
            RedisUtil redisUtil,
            ObjectMapper objectMapper,
            @Value("${app.cache.enabled:true}") boolean cacheEnabled,
            StringRedisTemplate stringRedisTemplate,
            DefaultRedisScript<Long> deductStockScript,
            StockAsyncService stockAsyncService,
            MeterRegistry meterRegistry,
            KafkaTemplate<String, StockDeductMessage> kafkaTemplate) {
        this.productMapper = productMapper;
        this.redisUtil = redisUtil;
        this.objectMapper = objectMapper;
        this.cacheEnabled = cacheEnabled;
        this.stringRedisTemplate = stringRedisTemplate;
        this.deductStockScript = deductStockScript;
        this.stockAsyncService = stockAsyncService;
        this.cacheHit = meterRegistry.counter("cache.hit");
        this.cacheMiss = meterRegistry.counter("cache.miss");
        this.deductSuccess = meterRegistry.counter("stock.deduct.success");
        this.deductFail = meterRegistry.counter("stock.deduct.fail");
        this.dbWriteFail = meterRegistry.counter("db.write.fail");
        this.kafkaTemplate = kafkaTemplate;
        this.asyncFail = meterRegistry.counter("stock.async.fail");
        this.asyncSuccess = meterRegistry.counter("stock.async.success");
        this.asyncRetry = meterRegistry.counter("stock.async.retry");
        this.asyncRollback = meterRegistry.counter("stock.async.rollback");
    }

    /*延迟双删所需代码段*/
    private final static long CACHE_DELAY_DELETE_MS = 300;
    private final ScheduledExecutorService cacheDeleteExecutor =
            Executors.newSingleThreadScheduledExecutor();

    private void delayDelete(String key) {
        cacheDeleteExecutor.schedule(() -> redisUtil.del(key),
                CACHE_DELAY_DELETE_MS,TimeUnit.MILLISECONDS);
    }
    /*延迟双删所需代码段*/

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
           /* if (cache instanceof Product) return (Product) cache;
            return objectMapper.convertValue(cache, Product.class);*/

            CacheData cacheData = objectMapper.convertValue(cache, CacheData.class);
            Object data = cacheData.getData();
            LocalDateTime expireTime = cacheData.getExpireTime();

            Product product = objectMapper.convertValue(data, Product.class);

            cacheHit.increment();//hit

            if(expireTime != null && expireTime.isAfter(LocalDateTime.now())) {
                return product;
            }

            if(expireTime == null) {
                return product;
            }

/*            if (expireTime.isBefore(LocalDateTime.now())) {
                CacheData newData = new CacheData();
                newData.setData(productMapper.selectById(id));
                newData.setExpireTime(LocalDateTime.now().plusSeconds(TTL_SECONDS));
                long ttl = TTL_SECONDS + ThreadLocalRandom.current().nextLong(TTL_JITTER_SECONDS + 1);
                redisUtil.set(key, newData, ttl);
            }*/

            /*添加互斥锁防止多次访问db*/
            if(expireTime.isBefore(LocalDateTime.now())) {
                String rebuildKey = "lock:rebuild:product:" + id;
                Boolean rebuildLock = stringRedisTemplate.opsForValue()
                        .setIfAbsent(rebuildKey, "1", 3, TimeUnit.SECONDS);

                if (Boolean.TRUE.equals(rebuildLock)) {
                    try {
                        Product fresh = productMapper.selectById(id);
                        if(fresh == null) {
                            redisUtil.set(key, RedisUtil.NULL_PLACEHOLDER, 30);
                        }else{
                            CacheData newData = new CacheData();
                            newData.setData(fresh);
                            newData.setExpireTime(LocalDateTime.now().plusSeconds(TTL_SECONDS));
                            redisUtil.set(key, newData
                                    , TTL_SECONDS +
                                            ThreadLocalRandom.current().nextLong(TTL_JITTER_SECONDS + 1));
                        }
                    }finally {
                        stringRedisTemplate.delete(rebuildKey);
                    }
                }
            }
            return product;
        }

        if(cache == null) cacheMiss.increment();//hit

        String lockKey = "lock:product:" + id;
        Boolean locked = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1",3, TimeUnit.SECONDS);

        if (Boolean.TRUE.equals(locked)) {
            try {
                Product db = productMapper.selectById(id);

                if(db == null){
                    redisUtil.set(key, RedisUtil.NULL_PLACEHOLDER, 30);
                    return null;
                }

                long ttl = TTL_SECONDS + ThreadLocalRandom.current().nextLong(TTL_JITTER_SECONDS + 1);

                /*redisUtil.set(key, db, ttl);*/
                /*
                    添加缓存过期
                */
                CacheData cacheData = new CacheData();
                cacheData.setData(db);
                cacheData.setExpireTime(LocalDateTime.now().plusSeconds(TTL_SECONDS));
                redisUtil.set(key, cacheData, ttl);

                return db;
            }finally {
                stringRedisTemplate.delete(lockKey);
            }
        }else{
            try {
                Thread.sleep(80);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Object retry = redisUtil.get(key);

            if (retry != null) {
                if(RedisUtil.NULL_PLACEHOLDER.equals(retry)) return null;
                if (retry instanceof Product) return (Product) retry;
                return objectMapper.convertValue(retry, Product.class);
            }

            return null;
        }

/*        Product db = productMapper.selectById(id);

        if (db == null) {
            redisUtil.set(key, RedisUtil.NULL_PLACEHOLDER,30);
            return null;
        }

        long ttl = TTL_SECONDS + ThreadLocalRandom.current().nextLong(TTL_JITTER_SECONDS + 1);

        redisUtil.set(key, db, ttl);

        return db;*/
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
            dbWriteFail.increment();
            throw new BusinessException(500,"创建失败");
        }

        if(cacheEnabled){
            String key = "product:" + product.getId();
            redisUtil.del(key);
            delayDelete(key);
        }

        stringRedisTemplate.opsForValue()
                .set("stock:"+product.getId(),String.valueOf(product.getStock()));

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
        if(request.getStock()!=null) {
            p.setStock(request.getStock());
            stringRedisTemplate.opsForValue().set("stock:"+ id,String.valueOf(request.getStock()));
        }

        p.setUpdatedAt(LocalDateTime.now());

        int rows = productMapper.updateById(p);

        if (rows != 1) {
            dbWriteFail.increment();
            throw new BusinessException(500,"更新失败");
        }

        if(cacheEnabled){
            String key = "product:" + p.getId();
            redisUtil.del(key);
            delayDelete(key);
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
            dbWriteFail.increment();
            throw new BusinessException(500, "删除失败");
        }

        if(cacheEnabled){
            String key = "product:" + p.getId();
            redisUtil.del(key);
            delayDelete(key);
            redisUtil.del("stock:"+ id);
        }

        return p;
    }

    @Transactional
    public boolean deductStock(Long id,int count) {
        String key = "stock:" + id;

        Long r = stringRedisTemplate.execute(deductStockScript,
                Collections.singletonList(key),
                String.valueOf(count));

        if(r == null || r == -1){
            deductFail.increment();
            throw new BusinessException(500,"库存没预热");
        }

        if(r == 0){
            deductFail.increment();
            throw new BusinessException(409,"库存不足");
        }

/*        if(asyncEnabled){
            stockAsyncService.writeBackAsync(id,count);
        }*/
        if(kafkaEnabled){
            String uuid = UUID.randomUUID().toString();

            StockDeductMessage msg = new StockDeductMessage();

            msg.setProductId(id);
            msg.setCount(count);
            msg.setTimestamp(LocalDateTime.now());
            msg.setRequestId(uuid);

            CompletableFuture<SendResult<String, StockDeductMessage>> result
                    = kafkaTemplate.send("stock_deduct", msg.getRequestId(), msg);

            result.whenComplete((res,ex) -> {
                if(ex != null){
                    asyncFail.increment();
                    log.error("kafka传递失败,requestId={}, productId={}, count={}", msg.getRequestId(), id, count,ex);
                    try {
                        stringRedisTemplate.opsForValue().increment("stock:" + id, count);
                    } catch (Exception e) {
                        log.error("redis回滚失败,requestId={}, productId={}", msg.getRequestId(), id, ex);
                    }
                }else{

                }
            });

        }
        else{
/*            int rows = productMapper.deductStock(id, count);

            if (rows != 1) {
                stringRedisTemplate.opsForValue().increment(key, count);
                throw new BusinessException(500,"库存回写失败");
            }*/
            //调用的mapper中deduct，改乐观锁后要用mp中update

            Product p = productMapper.selectById(id);
            if(p == null){
                deductFail.increment();
                throw new BusinessException(404,"商品不存在");
            }
            if(p.getStock() < count) {
                deductFail.increment();
                throw new BusinessException(409,"库存不足");
            }

            p.setStock(p.getStock() - count);

            int rows = productMapper.updateById(p); // 乐观锁生效
            if (rows != 1) {
                stringRedisTemplate.opsForValue().increment(key, count);
                deductFail.increment();
                throw new BusinessException(409, "并发冲突，请重试");
            }
            deductSuccess.increment();
        }

        return true;
    }

    @Override
    public boolean preheatStock(Long id) {
        Product p = productMapper.selectById(id);

        if(p == null){
            throw new BusinessException(404,"找不到该商品");
        }

        if(!cacheEnabled){
            throw new BusinessException(500,"未设定启动redis模块");
        }

        stringRedisTemplate.opsForValue().set("stock:"+ id,String.valueOf(p.getStock()));
        return true;
    }

    @KafkaListener(topics = "stock_deduct", groupId = "stock-writeback-group",concurrency = "3")
    public void onMessage(StockDeductMessage message) {
        String requestId = message.getRequestId();
        Long productId = message.getProductId();
        Integer count = message.getCount();

        log.info("Kafka收到消息 requestId={}, productId={}, count={}", requestId, productId, count);

        String key = "deduct:processed:" + requestId;
        String existed = stringRedisTemplate.opsForValue().get(key);
        if(existed != null) return;

        Product p = productMapper.selectById(productId);

        if(p == null){
            asyncFail.increment();
            log.warn("异步回写失败，未找到对应商品 id={}, count={}", productId, count);
            throw new RuntimeException("未找到对应商品");
        }

        if(p.getStock() < count){
            asyncFail.increment();
            log.warn("异步回写失败，商品数量不足 id={}, count={}", productId, count);
            throw new RuntimeException("商品数不足");
        }

        p.setStock(p.getStock()-count);
        int rows = productMapper.updateById(p);
        if(rows != 1){
            log.warn("异步回写失败，回滚redis库存量,id={},count={}", productId, count);
            asyncFail.increment();
            throw new RuntimeException("回写失败");
        }

        if(rows == 1){
            //asyncSuccess.increment();
            log.info("异步回写成功 id={}, count={}", productId, count);
            stringRedisTemplate.opsForValue()
                    .setIfAbsent("deduct:processed:" + requestId, "1", 1, TimeUnit.DAYS);
            asyncSuccess.increment();
            return;
        }
    }
}