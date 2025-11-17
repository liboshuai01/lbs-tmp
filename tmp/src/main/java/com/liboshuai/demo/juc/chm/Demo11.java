package com.liboshuai.demo.juc.chm;

import com.liboshuai.demo.function.FunctionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class Demo11 {

    private static final Logger log = LoggerFactory.getLogger(Demo11.class);

    // 1. Job 状态枚举
    enum JobState {
        CREATED,
        INITIALIZING,
        RUNNING,
        FAILED,
        FINISHED
    }

    // 2. 模拟的昂贵资源
    static class JobClassLoader {
        private final String jobId;

        public JobClassLoader(String jobId) {
            this.jobId = jobId;
            // 模拟昂贵的创建过程
            log.info("!!! 正在执行昂贵操作: 为 {} 创建 ClassLoader...", jobId);
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        public String getJobId() {
            return jobId;
        }

    }

    // 3. Job 指标 (一个可变对象)
    static class JobMetrics {
        private final AtomicLong processedRecords = new AtomicLong(0);

        public void addRecords(long count) {
            this.processedRecords.addAndGet(count);
        }

        public long getProcessedRecords() {
            return processedRecords.get();
        }

        @Override
        public String toString() {
            return "JobMetrics{" +
                    "processedRecords=" + processedRecords +
                    '}';
        }
    }

    // Map 1: 存储 Job 的当前状态
    private final ConcurrentHashMap<String, JobState> jobStates = new ConcurrentHashMap<>();
    // Map 2: 缓存 Job 的昂贵资源 (ClassLoader)
    private final ConcurrentHashMap<String, JobClassLoader> jobClassLoaders = new ConcurrentHashMap<>();
    /// Map 3: 存储 Job 的指标
    private final ConcurrentHashMap<String, JobMetrics> jobMetrics = new ConcurrentHashMap<>();

    // --- 模拟的 Job 管理器 ---

    // --- Main 方法: 模拟并发执行 ---
    public static void main(String[] args) throws InterruptedException {
        Demo11 manager = new Demo11();
        ExecutorService pool = Executors.newFixedThreadPool(10);

        String job1 = "job-001";
        String job2 = "job-002";

        // 1. 启动 Job-1 (正常流程)
        pool.submit(() -> {
            manager.registerJob(job1); // S1: putIfAbsent
            manager.initializeJob(job1); // S2: replace
            // 模拟 job 变为 FAILED
            manager.jobStates.put(job1, JobState.FAILED);
            System.out.println("[MainSim] Job " + job1 + " 状态设为 FAILED");
        });

        // 2. 启动 Job-2
        pool.submit(() -> {
            manager.registerJob(job2); // S1
            manager.initializeJob(job2); // S2
            manager.jobStates.put(job2, JobState.RUNNING); // 模拟
            System.out.println("[MainSim] Job " + job2 + " 状态设为 RUNNING");
        });

        // 3. 多个线程并发获取 Job-2 的 ClassLoader
        for (int i = 0; i < 5; i++) {
            pool.submit(() -> {
                manager.getOrCreateClassLoader(job2); // S3: computeIfAbsent
            });
        }

        // 4. 多个线程并发更新 Job-2 的指标
        for (int i = 0; i < 10; i++) {
            final long count = i;
            pool.submit(() -> {
                manager.addProcessedRecords(job2, count); // S4: computeIfPresent
            });
        }

        // 等待所有初始化任务完成
        Thread.sleep(2000);

        // 5. 报告和清理
        pool.submit(() -> {
            manager.reportMetrics(); // S6: mappingCount
            manager.cleanupFailedJobs(); // S5: 安全迭代
            System.out.println("\n--- 清理后 ---");
            manager.reportMetrics();
        });

        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);
    }

    /**
     * 场景 1: 当注册一个新 Job (Create)
     * 需求: 只有当 Job ID 不存在时, 才将其注册为 CREATED 状态.
     */
    private boolean registerJob(String jobId) {
        log.info("[S1] 尝试注册 Job: {}", jobId);

        // --- 常见的坑 (Check-then-Act- 竞态条件) ---
        // if (!jobStates.containsKey(jobId)) {
        //      // 危险! 在你检查后, put 之前, 另一个线程可能已经 put 了!
        //      jobStates.put(jobId, JobState.CREATED);
        // }

        // --- 正确选择: putIfAbsent (Demo2) ---
        // 它原子地完成了 "如果不存在,就放入"
        JobState oldState = jobStates.putIfAbsent(jobId, JobState.CREATED);
        if (oldState == null) {
            log.info("[S1] 注册成功 (Job {} -> CREATED)", jobId);
            return true;
        } else {
            log.info("[S1] 注册失败 (Job {} 已存在, 状态为 {})", jobId, oldState);
            return false;
        }
    }

    /**
     * 场景2: 状态转换 (Compare-and-Swap, CAS)
     * 需求: 只有当 Job "当前" 处于 CREATED 状态时, 才将其原子地变为 INITIALIZING.
     */
    private boolean initializeJob(String jobId) {
        log.info("[S2] 尝试初始化 Job: {}", jobId);

        /*
        --- 常见的坑 (Get-then-put 竞态条件) ---
        JobState currentState = jobStates.get(jobId);
        if (currentState == JobState.CREATED) {
            // 危险! 在你 get 和 put 之间, 另一个线程可能已将其变为 FAILED !
            jobStates.put(jobId, JobState.INITIALIZING);
        }

        --- 正确选择: replace(key, oldValue, newValue) (Demo3) ---
        // 它原子地完成了 "比较并替换"
         */
        boolean success = jobStates.replace(jobId, JobState.CREATED, JobState.INITIALIZING);
        if (success) {
            log.info("[S2] 初始化完成 (Job {} -> INITIALIZING)", jobId);
            return true;
        } else {
            log.info("[S2] 初始化失败 (Job {} 状态不是 CREATED", jobId);
            return false;
        }
    }

    /**
     * 场景3: 获取或创建昂贵资源 (Get-or-Create / Cache)
     * 需求: 获取 Job 的 ClassLoader. 如果缓存中没有, 则创建它. 并确保在并发时只创建一个实例.
     */
    private JobClassLoader getOrCreateClassLoader(String jobId) {
        log.info("[S3] 尝试获取 ClassLoader: {}", jobId);
        /*
            --- 常见的坑 (Check-then-Act) ---
            JobClassLoader cl = jobClassLoaders.get(jobId);
            if (cl == null) {
                cl = new JobClassLoader(jobId); // 昂贵操作
                // 危险! 两个线程可能都执行到这里, 创建两个 ClassLoader
                jobClassLoaders.put(jobId, cl);
            }
            return cl;
         */
        JobClassLoader loader = jobClassLoaders.computeIfAbsent(jobId, JobClassLoader::new);
        log.info("[S3] 成功获取 ClassLoader: {}", jobId);
        return loader;
    }

    /**
     * 场景4: 原子地更新指标 (Read-Modify-Write)
     * 需求: 为一个 "已存在" 的 Job 增加处理的记录数.
     */
    public void addProcessedRecords(String jobId, long count) {
//        jobMetrics.computeIfAbsent(jobId, k -> new JobMetrics());
//
//        jobMetrics.computeIfPresent(jobId, (id, metrics) -> {
//            metrics.addRecords(count);
//            log.info("[S4] 更新指标 (Job {}): {}", jobId, count);
//            return metrics;
//        });

        jobMetrics.compute(jobId, (id, metrics) -> {
            if (metrics == null) {
                log.info("[S4] 创建指标 (Job {}): {}", jobId, count);
                metrics = new JobMetrics();
            } else {
                log.info("[S4] 更新指标 (Job {}): {}", jobId, count);
            }
            metrics.addRecords(count);
            return metrics;
        });
    }

    /**
     * 场景5: 安全迭代与清理
     * 需求: 遍历所有 Job, 移除所有 "FAILED" 状态的 Job.
     */
    public void cleanupFailedJobs() {
        log.info("[S5] 开始清理 FAILED Jobs...");

        // --- 正确选择: 迭代器 或 keySet 遍历 (Demo 8) ---
        for (String jobId : jobStates.keySet()) {
            if (JobState.FAILED.equals(jobStates.get(jobId))) {
                // remove 是线程安全的
                if (jobStates.remove(jobId, JobState.FAILED)) {
                    log.info("[S5] 清理 FAILED Job: {}", jobId);
                    // 同时清理其他 Map
                    jobClassLoaders.remove(jobId);
                    jobMetrics.remove(jobId);
                }
            }
        }
    }

    /**
     * 场景6: 获取报告 (估算大小)
     * 需求: 报告当前有多少个 Job 在追踪
     */
    public void reportMetrics() {
        /*
            --- 常见的坑 ---
            int size = jobStates.size();
            if (size == 0) { ... }
            危险! size() 是估算值, 可能在并发时返回 0
         */
        // --- 正确选择: isEmpty() (Demo 10) ---
        if (jobStates.isEmpty()) {
            log.info("[S6] 报告: 当前没有 Job.");
            return;
        }

        // --- 正确选择: mappingCount() (Demo10) ---
        // 返回 long, 且明确了是 "估算值"
        long count = jobStates.mappingCount();
        log.info("[S6] 报告: 当前约有 {} 个 Job.", count);
        log.info("    - 状态: {}", jobStates);
        log.info("    - 指标: {}", jobMetrics);
    }

}
