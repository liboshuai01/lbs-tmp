package com.liboshuai.demo.rpc;

import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.japi.pf.ReceiveBuilder;
import org.apache.pekko.pattern.Patterns;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

// actor的功能，就是接收请求：请求可能来自于remote，也有可能来自于本地
// 一个actor实例，对应一个endpoint实例
public class PekkoRpcActor<T extends RpcEndpoint & RpcGateway> extends AbstractActor implements RpcActor {

    final T endpoint;

    public PekkoRpcActor(T endpoint){
        this.endpoint = endpoint;
    }


    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(RpcInvoke.class,this::invokeRpc)
                .match(RunAsync.class,this::invokeRunAsync)
                .build();
    }

    private void invokeRunAsync(RunAsync runAsync) {
        runAsync.getRunnable().run();
    }


    public void invokeRpc(RpcInvoke rpcInvoke) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        // rpc的调用发起方
        ActorRef sender = getSender();
        System.out.println("收到rpc调用请求,请求方: " + sender);

        String methodName = rpcInvoke.getMethodName();
        Class<?>[] parameterTypes = rpcInvoke.getParameterTypes();
        Object[] parameters = rpcInvoke.getParameters();

        Method method = endpoint.getClass().getMethod(methodName, parameterTypes);

        Object invokeResult = method.invoke(endpoint, parameters);

        // 处理细节：目标方法可能是void，也可能是有返回值的
        if(Objects.equals(method.getReturnType(),Void.TYPE)){

            System.out.println("没有返回值");
        }else if( invokeResult instanceof CompletableFuture){

            CompletableFuture<Object> completableFuture = (CompletableFuture<Object>) invokeResult;

            Patterns.pipe(completableFuture,getContext().getDispatcher()).to(sender);
        }

        else{
            System.out.println("准备返回结果: " + invokeResult);

            sender.tell(invokeResult,getSelf());
        }

    }



}
