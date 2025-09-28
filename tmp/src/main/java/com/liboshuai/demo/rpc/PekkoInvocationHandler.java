package com.liboshuai.demo.rpc;

import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.pattern.Patterns;
import scala.concurrent.Future;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class PekkoInvocationHandler implements InvocationHandler, RpcServer{

    private String address;
    private final ActorRef targetEndpointActorRef;

    public PekkoInvocationHandler(ActorRef targetEndpointActorRef) {
        this.targetEndpointActorRef = targetEndpointActorRef;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        // 构建rpc请求参数
        PekkoRpcInvocation rpcInvocation = new PekkoRpcInvocation();
        rpcInvocation.setMethodName(method.getName());
        rpcInvocation.setParameterTypes(method.getParameterTypes());
        rpcInvocation.setArgs(args);

        // 进行rpc请求，并获取响应值
        Object result = null;
        if (Objects.equals(method.getReturnType(), Void.TYPE)) {
            targetEndpointActorRef.tell(rpcInvocation, ActorRef.noSender());
        } else {
            Future<Object> scalaFuture = Patterns.ask(targetEndpointActorRef, rpcInvocation, 5000);
            CompletableFuture<Object> completableFuture = RpcUtils.convertScalaFuture2CompletableFuture(scalaFuture);
            if (Objects.equals(method.getReturnType(), CompletableFuture.class)) {
                result = completableFuture;
            } else {
                result = completableFuture.get();
            }
        }
        return result;
    }
}
