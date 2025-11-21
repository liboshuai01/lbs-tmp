package com.juc.rpc;

import com.juc.rpc.client.RpcClientProxy;
import com.juc.rpc.core.RpcContext;
import com.juc.rpc.server.RpcServer;
import com.juc.rpc.service.StockService;
import com.juc.rpc.service.StockServiceImpl; // 假设上面的实现类是 public 的或者在这里能访问

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class JucRpcApplication {

    public static void main(String[] args) throws InterruptedException {
        // 1. 注册服务
        RpcContext.registerService(StockService.class, new StockServiceImpl());

        // 2. 启动服务端
        RpcServer server = new RpcServer();
        server.start();

        // 3. 创建客户端代理
        StockService stockServiceProxy = RpcClientProxy.create(StockService.class);

        // 4. JUC: 准备高并发测试环境
        int threadCount = 100;
        // CountDownLatch: 闭锁。
        // 作用：让100个线程“预备”好，等主线程一声令下（countDown），同时发起请求。
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch finishLine = new CountDownLatch(threadCount);

        ExecutorService testPool = Executors.newCachedThreadPool();

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();

        log.info(">>> Starting Stress Test with {} threads...", threadCount);

        for (int i = 0; i < threadCount; i++) {
            testPool.execute(() -> {
                try {
                    // 所有线程在此等待，直到 startGun 为 0
                    startGun.await();

                    // 发起 RPC 调用
                    boolean result = stockServiceProxy.deductStock("C001", 1);
                    if (result) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    log.error("Call failed", e);
                    failCount.incrementAndGet();
                } finally {
                    // 完成任务，通知终点线
                    finishLine.countDown();
                }
            });
        }

        // 5. 只有这里调用了 countDown，上面的100个线程才会真正开始运行
        startGun.countDown();

        // 6. 主线程等待所有测试线程结束
        finishLine.await();

        long duration = System.currentTimeMillis() - startTime;
        log.info("<<< Test Finished in {} ms", duration);
        log.info("Success: {}, Fail: {}", successCount.get(), failCount.get());

        // 验证剩余库存
        // 这里其实是本地调用验证，但在真实RPC中应该是再发起一次RPC查询
        int remaining = stockServiceProxy.getStock("C001");
        log.info("Final Stock on Server: {}", remaining);

        System.exit(0);
    }
}