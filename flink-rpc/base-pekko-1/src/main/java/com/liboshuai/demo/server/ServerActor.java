package com.liboshuai.demo.server;

import com.liboshuai.demo.common.RequestData;
import com.liboshuai.demo.common.ResponseData;
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
                    log.info("收到客户端的消息，内容为：[{}]", requestData.getData());

                    String replyMessage = String.format("消息 [%s] 已收到!", requestData.getData());
                    ResponseData response = new ResponseData(replyMessage);

                    log.info("回应客户端的信息，内容为：[{}]", replyMessage);
                    getSender().tell(response, getSelf());
                })
                .matchAny(o -> log.warn("收到未知类型的消息: {}", o.getClass().getName()))
                .build();
    }
}