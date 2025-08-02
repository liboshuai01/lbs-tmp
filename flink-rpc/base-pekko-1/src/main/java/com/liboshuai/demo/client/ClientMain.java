package com.liboshuai.demo.client;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;

@Slf4j
public class ClientMain {
    public static void main(String[] args) {
        Config config = ConfigFactory.parseString(
                "pekko.remote.artery.canonical.port = 25531"
        ).withFallback(ConfigFactory.load());
        ActorSystem clientSystem = ActorSystem.create("clientSystem", config);

        String serverPath = "pekko://serverSystem@127.0.0.1:25530/user/serverActor";
        ActorRef actorRef = clientSystem.actorOf(ClientActor.props(serverPath));

        log.info("客户端 actor 已经创建完毕，完整路径为: {}", actorRef.path());
    }
}
