package com.liboshuai.demo.rpc;

import org.apache.pekko.actor.ActorSystem;

public class PekkoRpcService implements RpcService{

    private final ActorSystem actorSystem;

    public PekkoRpcService(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
    }

    @Override
    public <C extends RpcGateway> C connect(String address, Class<C> clazz) {
        return null;
    }

    @Override
    public String getAddress() {
        return "";
    }
}
