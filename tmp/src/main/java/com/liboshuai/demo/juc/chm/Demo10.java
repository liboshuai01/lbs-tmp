package com.liboshuai.demo.juc.chm;

import com.liboshuai.demo.function.FunctionUtils;
import com.liboshuai.demo.juc.chapter1.ExecutorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class Demo10 {

    private static final Logger log = LoggerFactory.getLogger(Demo10.class);

    public static void main(String[] args) throws InterruptedException {
        ConcurrentHashMap<Integer, String> map = new ConcurrentHashMap<>();

        // 1. 在 Map 稳定时, 两者相等
        log.info("--- 1. 稳定 Map ---");
        map.put(1, "A");
        map.put(2, "B");

        int stableSize = map.size();
        long stableMappingCount = map.mappingCount();

        log.info("Stable size(): {}", stableSize);
        log.info("Stable mappingCount(): {}", stableMappingCount);
        log.info("Stable isEmpty(): {}", map.isEmpty());

        // 2. 在 Map 并发修改时, 两者都是估算值
        log.info("");
        log.info("--- 2. 并发修改 Map ---");

        // 启动一个线程在后台疯狂添加元素
        ExecutorService pool = Executors.newSingleThreadExecutor();
        CompletableFuture<Void> cf = CompletableFuture.runAsync(FunctionUtils.uncheckedRunnable(
                () -> {
                    for (int i = 0; i < 10000000; i++) {
                        map.put(i, "Value-" + i);
                    }
                }
        ), pool);

        log.info("开始在并发修改时读取大小 (值会变化): ");

        // 我们不能保证读到的是哪个瞬时的值
        for (int i = 0; i < 5; i++) {
            TimeUnit.SECONDS.sleep(1);
            log.info("Snapshot {}: size={}, mappingCount={}", i, map.size(), map.mappingCount());
        }
        try {
            cf.get(10, TimeUnit.SECONDS);
            log.info("完成, 在规定时间内所有任务均执行完毕.");
        } catch (InterruptedException e) {
            log.warn("主线程在等待时被中断!");
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            log.error("至少有一个任务执行失败!", e);
        } catch (TimeoutException e) {
            log.warn("超时! 在规定时间内并非所有任务都执行完毕!");
        }
        ExecutorUtils.close(pool, 10, TimeUnit.SECONDS);

        log.info("");
        log.info("--- 3. 并发修改结束后 ---");
        // 当 Map 再次稳定时, 两者结果再次准确且相等
        log.info("Final size(): {}", map.size());
        log.info("Final mappingCount(): {}", map.mappingCount());

        // 3. size() vs isEmpty()
        // 为什么不用 size() == 0?
        // 因为 size() 在并发下是估算值, 它可能短暂地返回 0 (即使 Map 中有元素)
        // 或 > 0 (即使 Map 刚被清空)
        // isEmpty() 通常更快, 且能提供更准确的 "是否为空" 的即时判断
        log.info("");
        log.info("--- 4. 情况 Map ---");
        map.clear();
        log.info("After clear(), size(): {}", map.size());
        log.info("After clear(), mappingCount(): {}", map.mappingCount());
        log.info("After clear(), isEmpty(): {}", map.isEmpty());

    }
}
