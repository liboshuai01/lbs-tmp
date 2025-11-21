package com.juc.rpc.server;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.juc.rpc.core.RpcContext;
import com.juc.rpc.model.Protocol;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.concurrent.*;

@Slf4j
public class RpcServer {

    // JUC: volatile 保证线程可见性
    // 当主线程调用 shutdown() 将 isRunning 置为 false 时，Worker线程能立即感知到
    private volatile boolean isRunning = true;

    // JUC: 核心业务线程池
    // 这里的参数设计非常关键：
    // corePoolSize: 16 (模拟CPU密集型或IO密集型混合)
    // maxPoolSize: 32 (应对突发流量)
    // ArrayBlockingQueue: 有界队列，防止内存溢出 (OOM)
    // CallerRunsPolicy: 拒绝策略。当线程池满了，让提交任务的线程（即IO接收线程）自己去执行，
    //                   这会变相减慢IO接收速度，形成“反压”(Backpressure)机制。
    private final ThreadPoolExecutor businessExecutor;

    private final Thread ioThread;

    public RpcServer() {
        // JUC: 使用 Guava 的 ThreadFactoryBuilder 给线程命名，方便排查问题
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("rpc-worker-%d").build();

        this.businessExecutor = new ThreadPoolExecutor(
                16,
                32,
                60L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1000),
                namedThreadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        // 启动一个单线程模拟 NIO 的 Selector 轮询
        this.ioThread = new Thread(this::ioLoop, "rpc-io-thread");
    }

    public void start() {
        this.ioThread.start();
        log.info("RPC Server started successfully.");
    }

    private void ioLoop() {
        while (isRunning) {
            try {
                // JUC: 阻塞获取，没有请求时会让出CPU
                Protocol.RpcRequest request = RpcContext.takeRequest();

                // 提交给业务线程池处理，IO线程迅速返回去取下一个请求
                businessExecutor.execute(() -> process(request));

            } catch (InterruptedException e) {
                log.info("Server IO Interrupted");
                Thread.currentThread().interrupt();
            }
        }
    }

    private void process(Protocol.RpcRequest request) {
        Protocol.RpcResponse response = Protocol.RpcResponse.builder()
                .requestId(request.getRequestId())
                .build();

        try {
            // 1. 查找服务
            Object serviceImpl = RpcContext.getService(request.getClassName());
            if (serviceImpl == null) {
                throw new RuntimeException("Service not found: " + request.getClassName());
            }

            // 2. 反射调用
            Method method = serviceImpl.getClass().getMethod(request.getMethodName(), request.getParamTypes());
            Object result = method.invoke(serviceImpl, request.getParams());
            response.setResult(result);

        } catch (Throwable e) {
            log.error("Rpc Processing Error", e);
            response.setError(e);
        } finally {
            // 3. 回写结果 (模拟网络写出)
            RpcContext.sendResponse(response);
        }
    }
}