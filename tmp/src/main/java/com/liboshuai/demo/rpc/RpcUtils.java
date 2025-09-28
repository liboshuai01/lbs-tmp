package com.liboshuai.demo.rpc;

import com.typesafe.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.pekko.actor.ActorSystem;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RpcUtils {

    /**
     * 根据配置文件内容构建一个ActorSystem，并赋予PekkoRpcService
     */
    public static RpcService createRpcService(Configuration configuration) {
        // pekko://job-manager@127.0.0.1:18888/user/job-master
        String properties = configuration.getProperties("actor.system.name");
        ActorSystem actorSystem = ActorSystem.create(properties, ConfigFactory.load("application.conf").getConfig(properties));
        return new PekkoRpcService(actorSystem);
    }

    /**
     * 模拟远程rpc调用，网络产生的耗时，特意时间长一些，突显异步调用
     */
    public static void mockNetWorkTimeProcess(int timeout) {
        try {
            TimeUnit.SECONDS.sleep(timeout);
        } catch (InterruptedException e) {
            log.error("被中断，原因为：", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 将scala的future，转成java的CompletableFuture
     */
    public static <T> CompletableFuture<T> convertScalaFuture2CompletableFuture(Future<T> scalaFuture) {
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        scalaFuture.onComplete(scalaTry->{
            if(scalaTry.isSuccess()){
                completableFuture.complete(scalaTry.get());
            }else{
                completableFuture.completeExceptionally(scalaTry.failed().get());
            }
            return null;
        }, ExecutionContext.global());
        return completableFuture;
    }
}
