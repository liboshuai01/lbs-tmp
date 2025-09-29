package com.liboshuai.demo.rpc;

import lombok.extern.slf4j.Slf4j;
import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.Props;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

@Slf4j
public class PekkoRpcActor<T extends RpcEndpoint & RpcGateway> extends AbstractActor {

    protected final T endpoint;

    public PekkoRpcActor(T endpoint) {
        this.endpoint = endpoint;
    }

    public static <T extends RpcEndpoint & RpcGateway> Props props(T endpoint) {
        return Props.create(PekkoRpcActor.class, endpoint);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(PekkoRpcInvocation.class, this::invoke)
                .build();
    }

    private void invoke(PekkoRpcInvocation pekkoRpcInvocation) {
        ActorRef sender = getSender();
        log.info("接收到[{}]的rpc请求，请求参数为：{}", sender.path(), pekkoRpcInvocation);

        String methodName = pekkoRpcInvocation.getMethodName();
        Class<?>[] parameterTypes = pekkoRpcInvocation.getParameterTypes();
        Object[] args = pekkoRpcInvocation.getArgs();
        try {
            Method method = endpoint.getClass().getMethod(methodName, parameterTypes);
            Object result = method.invoke(endpoint, args);
            if (!Objects.equals(method.getReturnType(), Void.TYPE)) {
                sender.tell(result, getSelf());
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            log.info("反射执行方法出现异常", e);
        }
    }
}
