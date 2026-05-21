package com.xushu.rag.utils;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redisson工具类
 *
 * @author xushu
 * @date 2026-01-09
 */
@Slf4j
@Component
public class RedissonUtil {

    @Resource
    private RedissonClient redissonClient;

    // ============================== String 操作 ==============================

    /**
     * 设置键值对
     */
    public void set(String key, Object value) {
        RBucket<Object> bucket = redissonClient.getBucket(key);
        bucket.set(value);
    }

    /**
     * 设置键值对并指定过期时间
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        RBucket<Object> bucket = redissonClient.getBucket(key);
        bucket.set(value, timeout, unit);
    }

    /**
     * 获取值
     */
    public <T> T get(String key) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    /**
     * 删除键
     */
    public boolean delete(String key) {
        return redissonClient.getBucket(key).delete();
    }

    /**
     * 判断键是否存在
     */
    public boolean exists(String key) {
        return redissonClient.getBucket(key).isExists();
    }

    /**
     * 设置过期时间
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        return redissonClient.getBucket(key).expire(timeout, unit);
    }

    /**
     * 获取剩余过期时间
     */
    public long getExpire(String key) {
        return redissonClient.getBucket(key).remainTimeToLive();
    }

    // ============================== Hash 操作 ==============================

    /**
     * 设置Hash值
     */
    public void hset(String key, String field, Object value) {
        RMap<String, Object> map = redissonClient.getMap(key);
        map.put(field, value);
    }

    /**
     * 获取Hash值
     */
    public <T> T hget(String key, String field) {
        RMap<String, T> map = redissonClient.getMap(key);
        return map.get(field);
    }

    /**
     * 获取Hash所有键值对
     */
    public Map<String, Object> hgetAll(String key) {
        RMap<String, Object> map = redissonClient.getMap(key);
        return map.readAllMap();
    }

    /**
     * 删除Hash字段
     */
    public long hdel(String key, String... fields) {
        RMap<Object, Object> map = redissonClient.getMap(key);
        return map.fastRemove(fields);
    }

    /**
     * 判断Hash字段是否存在
     */
    public boolean hExists(String key, String field) {
        RMap<Object, Object> map = redissonClient.getMap(key);
        return map.containsKey(field);
    }

    /**
     * 获取Hash所有字段
     */
    public Set<Object> hKeys(String key) {
        RMap<Object, Object> map = redissonClient.getMap(key);
        return map.readAllKeySet();
    }

    /**
     * 获取Hash所有值
     */
    public Collection<Object> hValues(String key) {
        RMap<Object, Object> map = redissonClient.getMap(key);
        return map.readAllValues();
    }

    // ============================== List 操作 ==============================

    /**
     * 将值加入列表左侧
     */
    public void lpush(String key, Object... values) {
        RList<Object> list = redissonClient.getList(key);
        list.addAll(0, List.of(values));
    }

    /**
     * 将值加入列表右侧
     */
    public void rpush(String key, Object... values) {
        RList<Object> list = redissonClient.getList(key);
        list.addAll(List.of(values));
    }

    /**
     * 获取列表指定范围的元素
     */
    public List<Object> lrange(String key, int start, int end) {
        RList<Object> list = redissonClient.getList(key);
        return list.range(start, end);
    }

    /**
     * 获取列表长度
     */
    public int llen(String key) {
        RList<Object> list = redissonClient.getList(key);
        return list.size();
    }

    /**
     * 移除并返回列表左侧第一个元素
     */
    public Object lpop(String key) {
        RList<Object> list = redissonClient.getList(key);
        return list.remove(0);
    }

    /**
     * 移除并返回列表右侧最后一个元素
     */
    public Object rpop(String key) {
        RList<Object> list = redissonClient.getList(key);
        return list.remove(list.size() - 1);
    }

    /**
     * 根据索引获取列表元素
     */
    public Object lindex(String key, int index) {
        RList<Object> list = redissonClient.getList(key);
        return list.get(index);
    }

    // ============================== Set 操作 ==============================

    /**
     * 向Set添加元素
     */
    public boolean sadd(String key, Object... values) {
        RSet<Object> set = redissonClient.getSet(key);
        return set.addAll(List.of(values));
    }

    /**
     * 获取Set所有元素
     */
    public Set<Object> smembers(String key) {
        RSet<Object> set = redissonClient.getSet(key);
        return set.readAll();
    }

    /**
     * 判断Set中是否包含元素
     */
    public boolean sismember(String key, Object value) {
        RSet<Object> set = redissonClient.getSet(key);
        return set.contains(value);
    }

    /**
     * 获取Set大小
     */
    public int scard(String key) {
        RSet<Object> set = redissonClient.getSet(key);
        return set.size();
    }

    /**
     * 从Set中移除元素
     */
    public boolean srem(String key, Object... values) {
        RSet<Object> set = redissonClient.getSet(key);
        return set.removeAll(List.of(values));
    }

    // ============================== ZSet 操作 ==============================

    /**
     * 向有序集合添加元素
     */
    public boolean zadd(String key, double score, Object value) {
        RScoredSortedSet<Object> set = redissonClient.getScoredSortedSet(key);
        return set.add(score, value);
    }

    /**
     * 获取有序集合指定范围的元素
     */
    public Collection<Object> zrange(String key, int start, int end) {
        RScoredSortedSet<Object> set = redissonClient.getScoredSortedSet(key);
        return set.valueRange(start, end);
    }

    /**
     * 获取有序集合元素个数
     */
    public int zcard(String key) {
        RScoredSortedSet<Object> set = redissonClient.getScoredSortedSet(key);
        return set.size();
    }

    /**
     * 获取元素的分数
     */
    public double zscore(String key, Object value) {
        RScoredSortedSet<Object> set = redissonClient.getScoredSortedSet(key);
        return set.getScore(value);
    }

    /**
     * 移除有序集合中的元素
     */
    public boolean zrem(String key, Object... values) {
        RScoredSortedSet<Object> set = redissonClient.getScoredSortedSet(key);
        return set.removeAll(List.of(values));
    }

    // ============================== 分布式锁操作 ==============================

    /**
     * 获取分布式锁
     */
    public RLock getLock(String lockKey) {
        return redissonClient.getLock(lockKey);
    }

    /**
     * 尝试获取锁
     *
     * @param lockKey 锁的key
     * @param waitTime 等待时间
     * @param leaseTime 持有时间
     * @param unit 时间单位
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取分布式锁失败, lockKey: {}", lockKey, e);
            return false;
        }
    }

    /**
     * 释放锁
     */
    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    /**
     * 判断锁是否被持有
     */
    public boolean isLocked(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.isLocked();
    }

    // ============================== 其他操作 ==============================

    /**
     * 模糊查询key
     */
    public Iterable<String> keys(String pattern) {
        return redissonClient.getKeys().getKeysByPattern(pattern);
    }

    /**
     * 删除多个key
     */
    public long delete(String... keys) {
        return redissonClient.getKeys().delete(keys);
    }

    /**
     * 获取RedissonClient
     */
    public RedissonClient getRedissonClient() {
        return redissonClient;
    }
}
