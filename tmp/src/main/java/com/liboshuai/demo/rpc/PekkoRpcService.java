package com.liboshuai.demo.rpc;

public class PekkoRpcService implements RpcService{
    @Override
    public <C extends RpcGateway> C connect(String address, Class<C> clazz) {
        return null;
    }

    @Override
    public String getAddress() {
        return "";
    }
}
