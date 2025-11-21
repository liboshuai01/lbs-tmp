package com.juc.rpc.core;

import com.juc.rpc.model.Protocol;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

/**
 * RPC 上下文环境
 * 模拟网络环境（内存队列）和 注册中心（内存Map）
 */
@Slf4j
public class RpcContext {

    // 1. JUC: ConcurrentHashMap
    // 作用：服务注册表。支持高并发读（客户端寻找服务）和并发写（服务启动注册）。
    private static final Map<String, Object> SERVICE_REGISTRY = new ConcurrentHashMap<>();

    // 2. JUC: CompletableFuture + ConcurrentHashMap
    // 作用：存储正在处理中的请求。Key是RequestId。
    // 客户端发送请求后，生成一个Future放入此Map，然后阻塞等待（或异步回调）。
    // 服务端处理完后，通过RequestId找到这个Future，填入结果。
    private static final Map<Long, CompletableFuture<Protocol.RpcResponse>> PENDING_REQUESTS = new ConcurrentHashMap<>();

    // 3. JUC: LinkedBlockingQueue
    // 作用：模拟网络的“光缆”。
    // 这是一个生产者-消费者模型：客户端是生产者，服务端是消费者。
    // 使用阻塞队列可以天然地处理流量削峰，防止服务端被瞬时流量压垮。
    private static final BlockingQueue<Protocol.RpcRequest> NET_QUEUE = new LinkedBlockingQueue<>(10000);

    // --- 服务注册与发现 ---

    public static void registerService(Class<?> interfaceClass, Object serviceImpl) {
        SERVICE_REGISTRY.put(interfaceClass.getName(), serviceImpl);
        log.info("Service Registered: {}", interfaceClass.getName());
    }

    public static Object getService(String className) {
        return SERVICE_REGISTRY.get(className);
    }

    // --- 网络传输模拟 ---

    /**
     * 客户端发送请求
     */
    public static void sendRequest(Protocol.RpcRequest request, CompletableFuture<Protocol.RpcResponse> future) {
        PENDING_REQUESTS.put(request.getRequestId(), future);
        boolean success = NET_QUEUE.offer(request);
        if (!success) {
            future.completeExceptionally(new RejectedExecutionException("Network Queue is Full!"));
            PENDING_REQUESTS.remove(request.getRequestId());
        }
    }

    /**
     * 服务端获取请求 (阻塞式)
     */
    public static Protocol.RpcRequest takeRequest() throws InterruptedException {
        return NET_QUEUE.take(); // 如果队列为空，这里会阻塞，等待客户端请求
    }

    /**
     * 服务端回传响应
     */
    public static void sendResponse(Protocol.RpcResponse response) {
        CompletableFuture<Protocol.RpcResponse> future = PENDING_REQUESTS.remove(response.getRequestId());
        if (future != null) {
            // JUC: 这里的 complete 会唤醒正在等待结果的客户端线程
            future.complete(response);
        } else {
            log.warn("Response discarded (timeout or orphan): {}", response.getRequestId());
        }
    }
}