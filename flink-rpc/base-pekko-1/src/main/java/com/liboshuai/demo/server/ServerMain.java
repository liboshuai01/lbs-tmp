package com.liboshuai.demo.server;

import com.typesafe.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;

@Slf4j
public class ServerMain {
    public static void main(String[] args) {
        ActorSystem serverSystem = ActorSystem.create("serverSystem", ConfigFactory.load());

        ActorRef serverActor = serverSystem.actorOf(ServerActor.props(), "serverActor");

        log.info("服务端 actor 已经创建完毕，完整路径为: {}", serverActor.path());
    }
}
