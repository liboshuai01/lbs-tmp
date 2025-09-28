package com.liboshuai.demo.rpc;

import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSelection;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.actor.Address;

import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

public class PekkoRpcService implements RpcService {

    private final ActorSystem actorSystem;

    private final Map<String, ActorRef> actorRefMap = new HashMap<>();

    public PekkoRpcService(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
    }

    @Override
    public <C extends RpcGateway> C connect(String address, Class<C> clazz) {
        ActorSelection actorSel = actorSystem.actorSelection(address);
        CompletableFuture<ActorRef> completableFuture = actorSel.resolveOne(Duration.ofSeconds(5))
                .toCompletableFuture()
                .exceptionally(
                        error -> {
                            throw new CompletionException(String.format(
                                    "无法连接到地址%s下的RPC端点",
                                    address), error);
                        });
        ActorRef actorRef;
        try {
            actorRef = completableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        PekkoInvocationHandler handler = new PekkoInvocationHandler(actorRef);
        Class<?>[] interfaces = clazz.getInterfaces();
        @SuppressWarnings("unchecked")
        C gateway = (C) Proxy.newProxyInstance(clazz.getClassLoader(),interfaces,handler);
        return gateway;
    }

    @Override
    public <E extends RpcEndpoint & RpcGateway> RpcServer startServer(E endpoint) {
        // 使用 actorSystem 创建一个 actorRef
        ActorRef actorRef = actorSystem.actorOf(PekkoRpcActor.props(endpoint), endpoint.getEndpointId());

        // 为了后续管理
        actorRefMap.put(endpoint.getEndpointId(), actorRef);

        // 使用动态代理创建一个 RpcServer 的动态代理对象
        PekkoInvocationHandler handler = new PekkoInvocationHandler(actorRef);
        Class<?>[] interfaces = endpoint.getClass().getInterfaces();
        Set<Class<?>> interfaceSet = new HashSet<>(Arrays.asList(interfaces));
        interfaceSet.add(RpcServer.class);
        interfaces = interfaceSet.toArray(new Class<?>[0]);
        return (RpcServer) Proxy.newProxyInstance(RpcServer.class.getClassLoader(), interfaces, handler);
    }

    @Override
    public String getAddress(String endpointId) {
        ActorRef actorRef = actorRefMap.get(endpointId);
        Address address = actorSystem.provider().getDefaultAddress();
        return actorRef.path().toStringWithAddress(address);
    }
}
