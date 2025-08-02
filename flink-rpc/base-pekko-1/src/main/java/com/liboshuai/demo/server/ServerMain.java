package com.liboshuai.demo.server;

import com.typesafe.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;

import java.io.IOException;

@Slf4j
public class ServerMain {
    public static void main(String[] args) {
        ActorSystem serverSystem = ActorSystem.create("serverSystem", ConfigFactory.load());

        ActorRef serverActor = serverSystem.actorOf(ServerActor.props(), "serverActor");

        log.info("服务端 actor 已经创建完毕，完整路径为: {}", serverActor.path());

        log.info(">>> 按回车键退出 <<<");
        try {
            int ignored = System.in.read();
        } catch (IOException e) {
            log.error("等待输入时发生错误。", e);
        } finally {
            log.info("服务端正在关闭...");
            serverSystem.terminate();
        }
    }
}
