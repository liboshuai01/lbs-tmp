package com.liboshuai.demo.rpc;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.pekko.actor.ActorSystem;

public class RpcUtils {
    public static RpcService createRpcService(Configuration configuration) {

        // jobmanager /  taskmanager
        String actorSystemName = configuration.getProperty("actor.system.name");

        // 处理各种参数
        Config config = ConfigFactory.load();

        ActorSystem actorSystem = ActorSystem.create(actorSystemName, config.getConfig(actorSystemName));


        return new RpcService(actorSystem);
    }
}
