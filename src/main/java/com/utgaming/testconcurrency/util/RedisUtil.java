package com.utgaming.testconcurrency.util;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类，封装常用 Redis 操作
 * 支持 String、Hash、Set、List、有序 Set 操作及过期时间设置
 * 适合Spring Boot项目注入使用
 */
@Component
public class RedisUtil {
    public static final String NULL_PLACEHOLDER = "__NULL__";

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /*** ==================== 通用操作 ==================== ***/

    /**
     * 指定缓存失效时间，单位秒
     */
    public boolean expire(String key, long time) {
        try {
            if (time > 0) redisTemplate.expire(key, time, TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据key获取过期时间，单位秒，0代表永久有效
     */
    public long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 判断key是否存在
     */
    public boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除缓存，可批量
     */
    public void del(String... keys) {
        if (keys != null && keys.length > 0) {
            if (keys.length == 1) redisTemplate.delete(keys[0]);
            else redisTemplate.delete(CollectionUtils.arrayToList(keys).toString());
        }
    }

    /*** ==================== String 类型操作 ==================== ***/

    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean set(String key, Object value, long timeSeconds) {
        try {
            if (timeSeconds > 0) redisTemplate.opsForValue().set(key, value, timeSeconds, TimeUnit.SECONDS);
            else set(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public long incr(String key, long delta) {
        if (delta < 0) throw new RuntimeException("递增因子必须大于0");
        return redisTemplate.opsForValue().increment(key, delta);
    }

    public long decr(String key, long delta) {
        if (delta < 0) throw new RuntimeException("递减因子必须大于0");
        return redisTemplate.opsForValue().increment(key, -delta);
    }

    /*** ==================== Hash 类型操作 ==================== ***/

    public Object hget(String key, String item) {
        return redisTemplate.opsForHash().get(key, item);
    }

    public Map<Object, Object> hmget(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    public boolean hmset(String key, Map<String, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hmset(String key, Map<String, Object> map, long timeSeconds) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            if (timeSeconds > 0) expire(key, timeSeconds);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hset(String key, String item, Object value) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hset(String key, String item, Object value, long timeSeconds) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            if (timeSeconds > 0) expire(key, timeSeconds);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void hdel(String key, Object... items) {
        redisTemplate.opsForHash().delete(key, items);
    }

    public boolean hHasKey(String key, String item) {
        return redisTemplate.opsForHash().hasKey(key, item);
    }

    public double hincr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, by);
    }

    public double hdecr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, -by);
    }

    /*** ==================== Set 类型操作 ==================== ***/

    public Set<Object> sGet(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean sHasKey(String key, Object value) {
        try {
            return redisTemplate.opsForSet().isMember(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public long sSet(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public long sSetAndTime(String key, long timeSeconds, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            if (timeSeconds > 0) expire(key, timeSeconds);
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public long sGetSetSize(String key) {
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public long setRemove(String key, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().remove(key, values);
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /*** ==================== List 类型操作 ==================== ***/

    public List<Object> lGet(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public long lGetListSize(String key) {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public Object lGetIndex(String key, long index) {
        try {
            return redisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean lSet(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean lSet(String key, Object value, long timeSeconds) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            if (timeSeconds > 0) expire(key, timeSeconds);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean lSet(String key, List<Object> value) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean lSet(String key, List<Object> value, long timeSeconds) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            if (timeSeconds > 0) expire(key, timeSeconds);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean lUpdateIndex(String key, long index, Object value) {
        try {
            redisTemplate.opsForList().set(key, index, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public long lRemove(String key, long count, Object value) {
        try {
            return redisTemplate.opsForList().remove(key, count, value);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /*** ==================== 有序集合 ZSet 类型操作 ==================== ***/

    public boolean zSet(String key, Object value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    public long batchZSet(String key, Set<ZSetOperations.TypedTuple<Object>> tuples) {
        return redisTemplate.opsForZSet().add(key, tuples);
    }

    public void zIncrementScore(String key, Object value, long delta) {
        redisTemplate.opsForZSet().incrementScore(key, value, delta);
    }

    public void zUnionAndStore(String key, Collection<String> otherKeys, String destKey) {
        redisTemplate.opsForZSet().unionAndStore(key, otherKeys, destKey);
    }

    public long getZsetScore(String key, Object value) {
        Double score = redisTemplate.opsForZSet().score(key, value);
        return score == null ? 0 : score.longValue();
    }

    public Set<ZSetOperations.TypedTuple<Object>> getZSetRank(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
    }
    /*** ==================== 原子操作==================== ***/
    public Boolean setIfAbsent(String key, String value, long timeout, TimeUnit unit) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
    }
    /**
     * Hash自增1（无则初始化为1并设置过期时间）
     * @param key     Redis键（如："login:fail:count"）
     * @param hashKey Hash字段（如：用户名）
     * @param expire  过期时间（秒）
     * @return 自增后的值
     */
    public long hashIncrWithExpire(String key, String hashKey, long expire) {
        // 原子操作：字段值+1（不存在则自动初始化为0再+1=1）
        Long newVal = redisTemplate.opsForHash().increment(key, hashKey, 1);

        // 如果是新创建的字段（值为1），设置过期时间
        if (newVal != null && newVal == 1L) {
            redisTemplate.expire(key, expire, TimeUnit.SECONDS);
        }

        return newVal;
    }
}
