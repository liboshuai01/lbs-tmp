package com.liboshuai.demo.client;

import com.liboshuai.demo.common.RequestData;
import lombok.extern.slf4j.Slf4j;
import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.ActorSelection;
import org.apache.pekko.actor.Props;

@Slf4j
public class ClientActor extends AbstractActor {

    private String serverPath;
    private ActorSelection serverActorSelection;

    public ClientActor(String serverPath) {
        this.serverPath = serverPath;
        this.serverActorSelection = getContext().actorSelection(serverPath);
    }

    public static Props props(String serverPath) {
        return Props.create(ClientActor.class, () -> new ClientActor(serverPath));
    }

    @Override
    public void preStart() throws Exception, Exception {
        log.info("启动后自动向服务端发送信息，服务端地址为: [{}]", serverPath);
        serverActorSelection.tell(new RequestData("呼叫服务端，呼叫服务端"), getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchAny(o -> {
                    log.info("客户端接收到未知消息: {}", o);
                })
                .build();
    }
}
