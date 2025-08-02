package com.liboshuai.demo.client;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
public class ClientMain {
    public static void main(String[] args) {
        Config config = ConfigFactory.parseString(
                "pekko.remote.artery.canonical.port = 25531"
        ).withFallback(ConfigFactory.load());
        ActorSystem clientSystem = ActorSystem.create("clientSystem", config);

        String serverPath = "pekko://serverSystem@127.0.0.1:25530/user/serverActor";
        ActorRef clientActor = clientSystem.actorOf(ClientActor.props(serverPath), "clientActor");

        log.info("客户端 actor 已经创建完毕，完整路径为: {}", clientActor.path());
        log.info(">>> 请在控制台输入要发送的消息, 输入 'exit' 退出 <<<");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if ("exit".equalsIgnoreCase(line.trim())) {
                    break;
                }
                // Send the console input to the ClientActor
                clientActor.tell(line, ActorRef.noSender());
            }
        } catch (IOException e) {
            log.error("等待输入时发生错误。", e);
        } finally {
            log.info("客户端正在关闭...");
            clientSystem.terminate();
        }
    }
}