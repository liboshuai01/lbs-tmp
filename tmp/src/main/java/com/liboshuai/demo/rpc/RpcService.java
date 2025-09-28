package com.liboshuai.demo.rpc;

public interface RpcService {
    <G extends RpcGateway> G connect(String address, Class<G> clazz);

    <E extends RpcEndpoint & RpcGateway> RpcServer startServer(E endpoint);

    String getAddress(String endpointId);
}
