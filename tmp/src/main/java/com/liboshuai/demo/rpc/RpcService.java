package com.liboshuai.demo.rpc;

public interface RpcService {
    <G extends RpcGateway> G connect(String address, Class<G> clazz);

    <E extends RpcEndpoint & RpcGateway> E startServer(E endpoint, String endpointId);

    String getAddress();
}
