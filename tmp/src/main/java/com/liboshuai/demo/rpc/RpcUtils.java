package com.liboshuai.demo.rpc;

import com.typesafe.config.ConfigFactory;
import org.apache.pekko.actor.ActorSystem;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

import java.util.concurrent.CompletableFuture;

public class RpcUtils {
    public static RpcService createRpcService(Configuration configuration) {
        String actorSystemName = configuration.getProperties("actor.system.name");
        ActorSystem actorSystem = ActorSystem.create(actorSystemName, ConfigFactory.load("application.conf")
                .getConfig(actorSystemName));
        return new PekkoRpcService(actorSystem);
    }

    public static <T> CompletableFuture<T> convertScalaFuture2CompletableFuture(Future<T> scalaFuture) {
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        scalaFuture.onComplete(scalaTry -> {
            if (scalaTry.isSuccess()) {
                completableFuture.complete(scalaTry.get());
            } else if (scalaTry.isFailure()) {
                completableFuture.completeExceptionally(scalaTry.failed().get());
            }
            return null;
        }, ExecutionContext.global());
        return completableFuture;
    }
}
