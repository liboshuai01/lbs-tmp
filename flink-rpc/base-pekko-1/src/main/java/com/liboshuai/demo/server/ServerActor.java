package com.liboshuai.demo.server;

import com.liboshuai.demo.common.RequestData;
import lombok.extern.slf4j.Slf4j;
import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.Props;

@Slf4j
public class ServerActor extends AbstractActor {

    public static Props props() {
        return Props.create(ServerActor.class, ServerActor::new);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(RequestData.class, requestData -> {
                    log.info("接收到来自[{}]的消息，内容为：[{}]", getSender(), requestData.getData());
                })
                .matchAny(o -> log.warn("收到未知类型的消息: {}", o.getClass().getName()))
                .build();
    }
}
