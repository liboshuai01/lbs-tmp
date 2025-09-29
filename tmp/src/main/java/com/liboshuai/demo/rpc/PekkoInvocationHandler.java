package com.liboshuai.demo.rpc;

import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.pattern.Patterns;
import scala.concurrent.Future;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class PekkoInvocationHandler implements RpcServer, InvocationHandler {

    private final ActorRef actorRef;

    public PekkoInvocationHandler(ActorRef actorRef) {
        this.actorRef = actorRef;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        PekkoRpcInvocation pekkoRpcInvocation = new PekkoRpcInvocation(
                method.getName(),
                method.getParameterTypes(),
                args
        );
        Object result = null;
        if (Objects.equals(method.getReturnType(), Void.TYPE)) {
            actorRef.tell(pekkoRpcInvocation, ActorRef.noSender());
        } else {
            Future<Object> scalaFuture = Patterns.ask(actorRef, pekkoRpcInvocation, 5000);
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
