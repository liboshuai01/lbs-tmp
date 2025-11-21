package cn.liboshuai.demo.core.state.impl;

import cn.liboshuai.demo.config.EngineConfig;
import cn.liboshuai.demo.core.state.StateBackend;
import cn.liboshuai.demo.core.window.SlidingWindowCounter;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.params.ScanParams;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

/**
 * 基于 Redis 的状态后端
 * 生产级特性：
 * 1. 使用 JedisPool 连接池。
 * 2. snapshot 使用 Pipeline 批量写入，极大提升吞吐。
 * 3. load 使用 ForkJoinPool 并行 Scan + Get，加速启动恢复。
 */
@Slf4j
public class RedisStateBackend implements StateBackend {

    private final JedisPool jedisPool;
    private final String keyPrefix;

    public RedisStateBackend() {
        EngineConfig config = EngineConfig.getInstance();
        this.keyPrefix = config.getStateKeyPrefix();

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(20);
        poolConfig.setMaxIdle(10);
        poolConfig.setMinIdle(5);
        // 生产环境建议设置 TestOnBorrow 为 true
        poolConfig.setTestOnBorrow(true);

        this.jedisPool = new JedisPool(poolConfig, config.getRedisHost(), config.getRedisPort(), config.getRedisTimeout());
    }

    @Override
    public ConcurrentHashMap<String, SlidingWindowCounter> load() {
        log.info("Starting state recovery from Redis...");
        ConcurrentHashMap<String, SlidingWindowCounter> stateStore = new ConcurrentHashMap<>();
        long start = System.currentTimeMillis();

        try (Jedis jedis = jedisPool.getResource()) {
            // 1. SCAN 获取所有状态 Key (避免 KEYS 命令阻塞 Redis)
            ScanParams scanParams = new ScanParams().match(keyPrefix + "*");
            String cursor = ScanParams.SCAN_POINTER_START;

            // 简单的单线程 Scan 获取所有 Key，如果 Key 达到百万级，这里需要分片处理
            // 为了演示 JUC，我们假设获取到了 Key 集合，使用并行流加载 Value
            // 注意：实际生产海量 Key 不应一次加载到内存 Set，应流式处理
            // 这里为了代码清晰，演示并行加载逻辑

            Set<String> keys = jedis.keys(keyPrefix + "*"); // 仅作演示，生产禁用 keys *
            log.info("Found {} keys in Redis.", keys.size());

            if (!keys.isEmpty()) {
                // JUC 知识点: ForkJoinPool (Parallel Stream 底层) 并行拉取数据
                ForkJoinPool customPool = new ForkJoinPool(8); // 自定义并发度
                try {
                    customPool.submit(() ->
                            keys.parallelStream().forEach(redisKey -> {
                                try (Jedis j = jedisPool.getResource()) {
                                    String json = j.get(redisKey);
                                    if (json != null) {
                                        SlidingWindowCounter window = JSON.parseObject(json, SlidingWindowCounter.class);
                                        String bizKey = redisKey.substring(keyPrefix.length());
                                        stateStore.put(bizKey, window);
                                    }
                                } catch (Exception e) {
                                    log.error("Error loading key: " + redisKey, e);
                                }
                            })
                    ).get();
                } catch (Exception e) {
                    log.error("Parallel load failed", e);
                }
            }
        }

        log.info("State recovery finished. Loaded {} entries in {} ms.", stateStore.size(), System.currentTimeMillis() - start);
        return stateStore;
    }

    @Override
    public void snapshot(Map<String, SlidingWindowCounter> stateMap) {
        if (stateMap.isEmpty()) return;

        // JUC 知识点: 虽然 map 是并发安全的，但为了快照一致性，
        // 调用方应该保证在 snapshot 时尽量少写入，或者接受弱一致性。
        // 这里使用 Pipeline 批量写入
        try (Jedis jedis = jedisPool.getResource()) {
            Pipeline pipeline = jedis.pipelined();
            int batchSize = 0;

            for (Map.Entry<String, SlidingWindowCounter> entry : stateMap.entrySet()) {
                String redisKey = keyPrefix + entry.getKey();
                String json = JSON.toJSONString(entry.getValue());
                pipeline.set(redisKey, json);
                // 设置过期时间，防止僵尸数据 (例如 24小时)
                pipeline.expire(redisKey, 86400);

                batchSize++;
                if (batchSize >= 1000) {
                    pipeline.sync();
                    batchSize = 0;
                }
            }
            if (batchSize > 0) {
                pipeline.sync();
            }
        } catch (Exception e) {
            log.error("Snapshot to Redis failed", e);
            throw e; // 抛出异常由上层决定是否重试
        }
    }

    @Override
    public void close() {
        if (jedisPool != null) {
            jedisPool.close();
        }
    }
}