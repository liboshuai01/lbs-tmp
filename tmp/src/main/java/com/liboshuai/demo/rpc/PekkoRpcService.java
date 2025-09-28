package com.liboshuai.demo.rpc;

import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;

import java.lang.reflect.Proxy;
import java.util.*;

public class PekkoRpcService implements RpcService {

    private final ActorSystem actorSystem;

    private Map<String, ActorRef> actorRefMap;

    public PekkoRpcService(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
    }

    @Override
    public <C extends RpcGateway> C connect(String address, Class<C> clazz) {
        return null;
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
}
