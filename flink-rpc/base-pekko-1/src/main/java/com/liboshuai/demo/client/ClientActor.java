package com.liboshuai.demo.client;

import com.liboshuai.demo.common.RequestData;
import com.liboshuai.demo.common.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.ActorSelection;
import org.apache.pekko.actor.Props;

@Slf4j
public class ClientActor extends AbstractActor {

    private final ActorSelection serverActorSelection;

    public ClientActor(String serverPath) {
        this.serverActorSelection = getContext().actorSelection(serverPath);
    }

    public static Props props(String serverPath) {
        return Props.create(ClientActor.class, () -> new ClientActor(serverPath));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, message -> {
                    // Message from console input in ClientMain
                    log.info("向服务端发送消息: [{}]", message);
                    serverActorSelection.tell(new RequestData(message), getSelf());
                })
                .match(ResponseData.class, response -> {
                    // Message received from the server
                    log.info("接收到服务端响应: [{}]", response.getResponse());
                })
                .matchAny(o -> log.warn("客户端接收到未知消息: {}", o.getClass().getName()))
                .build();
    }
}