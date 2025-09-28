package com.liboshuai.demo.rpc;

import lombok.extern.slf4j.Slf4j;
import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.Props;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class PekkoRpcActor<C extends RpcEndpoint & RpcGateway> extends AbstractActor {

    private final C endpoint;

    public PekkoRpcActor(C endpoint) {
        this.endpoint = endpoint;
    }

    public static <C extends RpcEndpoint & RpcGateway> Props props(C endpoint) {
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
        log.info("接收到rpc调用，发送人：[{}]", sender);

        String methodName = pekkoRpcInvocation.getMethodName();
        Class<?>[] parameterTypes = pekkoRpcInvocation.getParameterTypes();
        Object[] args = pekkoRpcInvocation.getArgs();

        try {
            Method method = endpoint.getClass().getMethod(methodName, parameterTypes);
            Object result = method.invoke(endpoint, args);
            // 返回正确的结果
            sender.tell(result, getSelf());
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            log.error("通过反射执行方法时出现异常", e);
            // 返回表示错误的结果
            sender.tell("执行方法错误", getSelf());
        }
    }
}
