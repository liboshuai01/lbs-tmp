package com.juc.rpc.client;

import com.juc.rpc.core.RpcContext;
import com.juc.rpc.model.Protocol;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class RpcClientProxy {

    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> interfaceClass) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new RpcInvocationHandler()
        );
    }

    static class RpcInvocationHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // 1. 构建请求
            Protocol.RpcRequest request = Protocol.RpcRequest.builder()
                    .requestId(Protocol.nextId())
                    .className(method.getDeclaringClass().getName())
                    .methodName(method.getName())
                    .paramTypes(method.getParameterTypes())
                    .params(args)
                    .timestamp(System.currentTimeMillis())
                    .build();

            // 2. 创建 Future 句柄
            CompletableFuture<Protocol.RpcResponse> future = new CompletableFuture<>();

            // 3. 发送请求 (推入队列)
            RpcContext.sendRequest(request, future);

            // 4. 同步等待结果
            // JUC: 这里演示的是同步调用，利用 get() 阻塞。
            // 如果想做纯异步，可以暴露 Future 给上层。
            try {
                // 设置超时时间，防止网络丢包导致永久阻塞
                Protocol.RpcResponse response = future.get(5, TimeUnit.SECONDS);

                if (response.hasError()) {
                    throw response.getError();
                }
                return response.getResult();
            } catch (Exception e) {
                throw new RuntimeException("RPC Call Failed", e);
            }
        }
    }
}