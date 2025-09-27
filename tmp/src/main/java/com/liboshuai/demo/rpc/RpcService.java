package com.liboshuai.demo.rpc;

public interface RpcService {
    <C extends RpcGateway> C connect(String address, Class<C> clazz);

    String getAddress();
}
