package com.liboshuai.demo.rpc;

import lombok.Getter;

public abstract class RpcEndpoint implements RpcGateway {

    @Getter
    private final RpcService rpcService;
    @Getter
    private final String endpointId;
    @Getter
    private final RpcServer rpcServer;

    public RpcEndpoint(RpcService rpcService, String endpointId) {
        this.rpcService = rpcService;
        this.endpointId = endpointId;

        rpcServer = rpcService.startServer(this);
    }
}
